import csv
from io import StringIO
from minio import Minio
import os
import logging

import pymssql

minio_access_key=os.environ.get('MINIO_ACCESS_KEY')
minio_secret_key=os.environ.get('MINIO_SECRET_KEY')
minio_endpoint=os.environ.get('MINIO_ENDPOINT')
minio_secure=os.environ.get('MINIO_SECURE', False)
csv_file_name=os.environ.get('CSV_FILE_NAME')
csv_bucket_name=os.environ.get('CSV_BUCKET_NAME')

db_server=os.environ.get('DB_SERVER')
db_user=os.environ.get('DB_USER')
db_password=os.environ.get('DB_PASSWORD')
db_name=os.environ.get('DB_NAME')
table_name=os.environ.get('TABLE_NAME')

tmp_folder=os.environ.get('TMP_FOLDER','')

numeric_level = getattr(logging, os.getenv('LOG_LEVEL').upper(), 10)
# Set up logging
logging.basicConfig(
    level=numeric_level,
    format='%(asctime)s %(levelname)s %(module)s:%(lineno)d [RIDE_GEOLOCATION]: %(message)s'
)

if minio_secure:
    cert_path=os.environ.get('CERT_PATH')
    os.environ['SSL_CERT_FILE'] = cert_path

minio_client = Minio(
    minio_endpoint,  # e.g., "localhost:9000"
    access_key=minio_access_key,
    secret_key=minio_secret_key,
    secure=minio_secure  # Set to True if using HTTPS
)

conn = pymssql.connect(server=db_server, user=db_user, password=db_password, database=db_name)


def db_insert(row):
    cursor = conn.cursor()
    qry_str = f'INSERT INTO {table_name} ([business_program],[business_type],[business_id], [lat], [long], [databc_long], [databc_lat]) VALUES (%s, %s, %s, %s, %s, %s, %s)'
    cursor.execute(qry_str,
        ('Test', 'Testing', row['business_id'], row['lat'], row['long'], row['databc_long'], row['databc_lat'])
    )
    conn.commit()

def db_updates(row):
    cursor = conn.cursor()
    databc_value=''
    qry_str=f'UPDATE {table_name} SET [lat] = %s, [long] = %s,[databc_long]=%s,[databc_lat]=%s WHERE [business_id] = %s'
    cursor.execute(qry_str,
        (row['lat'][:15], row['long'][:15], databc_value,databc_value,row['ticket'])
    )
    conn.commit()

def data_update_from_csv():
    try:
        logging.info("Process started to update data from csv file")
        object_name = csv_file_name
        bucket_name = csv_bucket_name
        data = minio_client.get_object(bucket_name, object_name)
        csv_content = StringIO(data.read().decode('utf-8'))
        reader = csv.DictReader(csv_content)
    except Exception as e:
        logging.error("Error while reading csv file")
        logging.error(e)
        return False
    for row in reader:
        try:
            logging.info(f"Processing row: {row['ticket']}")
            logging.debug("Row Details: %s", row)
            # print("date:", row['date'])
            # print("ticket:", row['ticket'])
            # print("lat:", row['lat'])
            # print("long:", row['long'])
            # print("------")  # Separator for clarity
            db_updates(row)
            logging.info(f"Row processed: {row['ticket']}")
            # update to minio bucket for processed records to a csv file
            processed_csv_file_name = f'{tmp_folder}processed-{csv_file_name}'
            write_csv_records(row,processed_csv_file_name,reader.fieldnames)
            # read file in csv_data
            with open(processed_csv_file_name, 'rb') as file_data:
                file_stat = os.stat(processed_csv_file_name)
                minio_client.put_object(
                    bucket_name, 
                    processed_csv_file_name, 
                    file_data, 
                    file_stat.st_size
                )
                # csv_data = f.read()
                # csv_data = StringIO(bytes(f.read(),'utf-8'))


            # csv_data = open(processed_csv_file_name, 'r')
            # csv_data = csv_data.read()
            # csv_data = StringIO(csv_data)
            # csv_data = StringIO(csv_data)

            # csv_data = StringIO()
            # csv_writer = csv.DictWriter(csv_data, fieldnames=reader.fieldnames)
            # csv_writer.writeheader()
            # csv_writer.writerow(row)
            # csv_data.seek(0)
            # upload processed_csv_file_name file to minio
            # minio_client.put_object(bucket_name, processed_csv_file_name, csv_data.getvalue(), len(csv_data.getvalue()))
            
            logging.info(f"Processed row: {row['ticket']} updated to minio bucket")

            logging.info("------------------")
        except Exception as e:
            logging.error(f"Error while processing row: {row['ticket']}")
            logging.error(e)
            logging.info("------------------")
    # remove file
    try:
        os.remove(processed_csv_file_name)
    except Exception as e:
        logging.error(f"Error while removing file: {processed_csv_file_name}")
        logging.error(e)
    logging.info("Process completed to update data from csv file")

def write_csv_records(row,csv_file_name,headers):
    try:
        with open(csv_file_name, 'r') as f:
            pass
    except FileNotFoundError:
        # Create the file and write the headers if it doesn't exist
        with open(csv_file_name, 'w') as f:
            writer = csv.DictWriter(f, fieldnames=headers)
            writer.writeheader()
    with open(csv_file_name, 'a') as f:
        writer = csv.DictWriter(f, fieldnames=headers)
        writer.writerow(row)


if __name__ == "__main__":
    # valstr='123456789'
    # val_split=valstr[:30]
    # print(val_split)
    data_update_from_csv()
