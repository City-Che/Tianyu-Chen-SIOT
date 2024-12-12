#include <Wire.h>
#include <Adafruit_AMG88xx.h>
#include "MAX30105.h"
#include "heartRate.h"

// 初始化传感器对象
Adafruit_AMG88xx amg;
MAX30105 particleSensor;

// 温度检测参数
float pixels[64];          // 存储 8x8 温度矩阵
float humanTempThreshold = 28.0; // 人体温度最低阈值 (摄氏度)

// 心率检测参数
long lastBeat = 0;         // 上一次心跳的时间戳
float beatsPerMinute;
const int windowSize = 16;
long irValues[windowSize] = {0};
int irIndex = 0;
int sleepindex = 0;

// 用于睡眠阶段检测的变量
float hrv = 0;                  // 心率变异性
const int hrvWindowSize = 5;
float beatIntervals[hrvWindowSize] = {0}; // 保存最近几次心跳间隔
int hrvIndex = 0;
long loopStartTime = 0;

void setup() {
  Serial.begin(115200);

  // 初始化 AMG8833 温度传感器
  if (!amg.begin()) {
    Serial.println("AMG8833 传感器初始化失败，请检查连接!");
    while (1);
  }
  delay(100);  // 传感器初始化延时

  // 初始化 MAX30102 心率传感器
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("MAX30102 初始化失败，请检查连接!");
    while (1);
  }
  particleSensor.setup();
  particleSensor.setPulseAmplitudeRed(0x3F);
  particleSensor.setPulseAmplitudeGreen(0);
  
  delay(2000);  // 延时2秒，确保传感器稳定
}

long previousIR = 0;
long beatThreshold = 800;  // 设定心跳波动阈值

// 计算 HRV的函数
float calculateHRV() {
  float meanInterval = 0;
  for (int i = 0; i < hrvWindowSize; i++) {
    meanInterval += beatIntervals[i];
  }
  meanInterval /= hrvWindowSize;

  float sumSqDiff = 0;
  for (int i = 0; i < hrvWindowSize; i++) {
    sumSqDiff += pow(beatIntervals[i] - meanInterval, 2);
  }
  return sqrt(sumSqDiff / hrvWindowSize);
}

void loop() {
  // 1. 检测人体存在
  amg.readPixels(pixels);
  float humanTempSum = 0;
  int humanTempCount = 0;
  bool humanDetected = false;

  for (int i = 0; i < 64; i++) {
        if (pixels[i] > humanTempThreshold) {
      humanTempSum += pixels[i];
      humanTempCount++;
      humanDetected = true;
    }
  }

  float humanTempAverage = humanDetected ? humanTempSum / humanTempCount : NAN;
  if (humanTempAverage > 40) {
    humanTempAverage = 40; // 限制最大温度值为40
  }
  else if (humanTempAverage < 30) {
    humanTempAverage = 30; // 限制最小温度值为30
  }

  // 计算 Tindex
  int Tindex = (humanTempAverage < 36) ? 1 : 0;

  // 2. 检测心率并计算睡眠阶段
  long irValue = particleSensor.getIR();
  irValues[irIndex] = irValue;
  irIndex = (irIndex + 1) % windowSize;
  long filteredIR = 0;

  for (int i = 0; i < windowSize; i++) {
    filteredIR += irValues[i];
  }
  filteredIR /= windowSize;

  if (abs(filteredIR - previousIR) > beatThreshold) {
    long currentBeat = millis();
    if (lastBeat > 0) {
      float beatDuration = (currentBeat - lastBeat) / 1000.0;
        beatsPerMinute = 60 / beatDuration;

        // 限制心率在50到100之间
        if (beatsPerMinute < 50) {
          beatsPerMinute = 50;
        } 
        else if (beatsPerMinute > 90) {
          beatsPerMinute = 90;
        }

        // 更新心跳间隔数组，用于计算 HRV
        beatIntervals[hrvIndex] = beatDuration;
        hrvIndex = (hrvIndex + 1) % hrvWindowSize;

        // 计算 HRV
        hrv = calculateHRV();

        // 判断睡眠阶段
        if (beatsPerMinute < 60 && hrv < 0.7) {  // 深度睡眠
          sleepindex = 1;
        } else {  // 浅层睡眠
          sleepindex = 0;
        }
      
    }
    lastBeat = currentBeat;
  }
  previousIR = filteredIR;

  // 3. 输出格式化的数据
  Serial.print(humanDetected ? humanTempAverage : NAN); // 温度数据
  Serial.print(",");
  Serial.print(beatsPerMinute);  // 心率
  Serial.print(",");
  Serial.print(hrv);  // HRV
  Serial.print(",");
  Serial.print(sleepindex);  // 睡眠阶段
  Serial.print(",");
  Serial.println(Tindex);  // Tindex
  
  delay(12000);  // 延时 120 秒
}