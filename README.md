                自定义折线图
* 大致思路：
* 1.初始化画笔和路径等，设置模拟数据等
* 2.获取activity中设置的数据信息
* 3.在onMeasure()中设置宽高百分比等信息(根据数据将宽度等分，可用高度由外部传入（以百分比形式占据原控件的高），用数据的最大y和最小y的差值作为百分比等比例划分)
* 4.算出每个点的坐标，绘制点，线和阴影并连线
* 注意:
* 1.当文字宽度超过10*2个像素时，第一个点的内容会从paddingLeft开始，最后一个点会到paddingRight结束
* 2.点需在阴影上方，要么将点作为前景绘制，要么画点在画阴影之后

成品图片：https://github.com/fenchen31/picture/blob/master/LineChartView.jpg
或者项目路径下 src/main/res/drawable/img.png