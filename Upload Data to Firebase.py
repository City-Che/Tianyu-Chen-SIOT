import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

# 初始化 Firebase
cred = credentials.Certificate('D:/SIOT/firebase_service_account_key.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://your-database-name.firebaseio.com'  # Firebase数据库的URL
})

# 获取 Firestore 客户端
db = firestore.client()

# 读取 CSV 文件
csv_file_path = 'D:/SIOT/sensor_data.csv'
data = pd.read_csv(csv_file_path)

# 将数据上传到 Firestore
for index, row in data.iterrows():
    db.collection('sensor_data').add({
        'Temperature': row['Temperature'],
        'HeartRate': row['HeartRate'],
        'HRV': row['HRV'],
        'SleepIndex': row['SleepIndex'],
        'Tindex': row['Tindex']
    })
print("Data uploaded to Firebase successfully!")
