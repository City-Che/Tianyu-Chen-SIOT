import serial
import csv
import time

# 配置串口参数
SERIAL_PORT = "COM6"  # 替换为你的Arduino连接的串口号
BAUD_RATE = 115200  # 确保与Arduino的代码一致
CSV_FILE_PATH = "D:/SIOT/sensor_data.csv"  # 替换为你希望保存CSV的路径

def main():
    # 打开串口
    try:
        ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
        print(f"正在连接到 {SERIAL_PORT}，波特率 {BAUD_RATE}...")
    except Exception as e:
        print(f"无法连接到串口: {e}")
        return

    # 打开CSV文件，准备写入数据
    try:
        with open(CSV_FILE_PATH, mode="a", newline="") as csv_file:
            writer = csv.writer(csv_file)
            # 如果是新文件，写入表头
            if csv_file.tell() == 0:
                writer.writerow(["Temperature", "HeartRate", "HRV", "SleepIndex", "Tindex"])

            print("开始接收数据，按 Ctrl+C 结束...")
            while True:
                try:
                    # 从串口读取一行数据
                    line = ser.readline().decode("utf-8").strip()
                    if line:
                        # 将数据分割为列表
                        data = line.split(",")
                        if len(data) == 5:  # 确保数据完整
                            writer.writerow(data)  # 写入CSV
                            print(f"数据写入成功: {data}")
                        else:
                            print(f"收到的数据格式不正确: {line}")
                except KeyboardInterrupt:
                    print("\n接收中断，程序退出。")
                    break
                except Exception as e:
                    print(f"读取数据时出错: {e}")

    except Exception as e:
        print(f"无法打开CSV文件: {e}")
    finally:
        ser.close()

if __name__ == "__main__":
    main()

