import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import pearsonr

file_path = r'D:\SIOT\sensor_data.csv'

# 1. 加载数据
def load_data(file_path):
    # 读取 CSV 数据
    data = pd.read_csv(file_path)

    # 确保有 'Temperature', 'HeartRate' 两列
    if not all(col in data.columns for col in ['Temperature', 'HeartRate']):
        raise ValueError("CSV 文件中需要包含 'Temperature', 'HeartRate' 列")

    # 取最近 400 行数据
    recent_data = data.tail(400)

    return recent_data

# 2. 绘制时间序列趋势图
def plot_time_series(data):
    plt.figure(figsize=(12, 6))

    # 绘制体温曲线
    plt.plot(data.index, data['Temperature'], label='Temperature (°C)', color='blue', marker='o', markersize=4, alpha=0.7)

    # 绘制心率曲线
    plt.plot(data.index, data['HeartRate'], label='Heart Rate (bpm)', color='red', marker='x', markersize=4, alpha=0.7)

    # 图表标题和标签
    plt.title('Trend of Temperature and Heart Rate (Last 400 Samples)', fontsize=16)
    plt.xlabel('Sample Index', fontsize=12)
    plt.ylabel('Values', fontsize=12)
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.show()

# 3. 计算 Pearson 相关系数
def calculate_correlation(data):
    # 获取体温和心率的列
    temperature = data['Temperature']
    heart_rate = data['HeartRate']

    # 计算 Pearson 相关系数
    correlation, p_value = pearsonr(temperature, heart_rate)

    print(f"Pearson Correlation Coefficient: {correlation:.2f}")
    print(f"P-value: {p_value:.4f}")
    if p_value < 0.05:
        print("Result: There is a statistically significant correlation.")
    else:
        print("Result: No statistically significant correlation.")

# 4. 主函数
def main():
    file_path = r'D:\SIOT\sensor_data.csv'  # 替换为你的 CSV 文件路径
    data = load_data(file_path)

    print("Loaded data (last 5 rows):")
    print(data.tail())

    # 绘制时间序列趋势图
    plot_time_series(data)

    # 计算 Pearson 相关系数
    calculate_correlation(data)

# 执行主函数
if __name__ == "__main__":
    main()
