# RingTimeSelectView
A multi-selector for time between 0 and 60 minute
一个分钟时间段的选择器，可选择多个

## 操作演示
![demoGif](https://github.com/zjun615/RingTimeSelectView/blob/master/img/demo.gif)

## 主要功能
 - 在0~60之间，选择一个或多个时间段
 - 添加：点击圆环空白处添加时间段
 - 删除：起始点和终止点在同一时间点
 - 修改：滑动起始点或终止点
 
## 其他效果
![more](https://github.com/zjun615/RingTimeSelectView/blob/master/img/more.png) 


## 属性说明

属性名 | 说明 | 默认值
:------ | :------ | :------
rtv_startMinute    | 默认第一组的起始时间（∈[0, 60]，与rtv_endMinute一起使用，可用于在布局中显示效果。-1代表无效值） | -1
rtv_endMinute    | 默认第一组的终止时间（∈[0, 60], 与rtv_startMinute一起使用，可用于在布局中显示效果。-1代表无效值）| -1
rtv_gravity    | 重力，代表对齐方式，与layout_gravity差不多。共有6种可组合使用的标记：top、bottom、center_vertical、left、right、center_horizontal、center | top|left
rtv_initialMinutes    | 初始化时间间隔，创建新时间段时，时间段的间隔值 | 5
rtv_ringWidth    | 圆环的宽度 | 30dp
rtv_ringBgColor    | 圆环背景色 | #a7a7a7（淡灰色）
rtv_sectionSum    | 可创建时间段的总个数 | 3
rtv_quickCutEnable    | 是否开启快速剪切功能。开启后，在已选时间段上点击，能快速修改终止时间为点击的时间点 | false
rtv_sectionColor    | 已选时间段的颜色 | #148c75（深绿色）
rtv_sectionColor2    | 已选时间的渐变色2。设置后，已选时间段将是渐变色，由rtv_sectionColor、rtv_sectionColor2一起组合 | -1
rtv_sectionColor3    | 已选时间的渐变色3。设置后，已选时间段将是渐变色，由rtv_sectionColor、rtv_sectionColor2、rtv_sectionColor3一起组合 | -1
rtv_anchorDiameter    | 锚点（时间段的两个端点）的直径 | 50dp
rtv_anchorStrokeWidth    | 锚点描边宽度 | 6dp
rtv_anchorTextSize    | 锚点字体大小 | 16sp
rtv_anchorNeedMerge    | 起始锚点为0，终止锚点为60时，是否需要合并 | true
rtv_anchorStartColor    | 起始锚点的颜色 | #007ffe（淡蓝色）
rtv_anchorStartStrokeColor    | 起始锚点的描边颜色 | #FFFFFF（白色）
rtv_anchorStartText    | 起始锚点的文字 | ON
rtv_anchorStartTextColor    | 起始锚点的文字颜色 | #FFFFFF（白色）
rtv_anchorEndColor    | 终止锚点的颜色 | （同rtv_anchorStartColor）
rtv_anchorEndStrokeColor    | 终止锚点的描边颜色 | （同rtv_anchorStartStrokeColor）
rtv_anchorEndText    | 终止锚点的文字 | OFF
rtv_anchorEndTextColor    | 终止锚点的文字颜色 | （同rtv_anchorStartTextColor）
rtv_degreeColor    | 刻度颜色 | #888888（深灰色）
rtv_degreeLongLength    | 长刻度（能被5整除的时间点）长度 | （圆环中心圆的1/16）
rtv_degreeLongWidth    | 长刻度宽度 | 2dp
rtv_degreeShortLength    | 短刻度长度 | rtv_degreeLongLength的1/2
rtv_degreeShortWidth    | 短刻度宽度 | （同rtv_degreeLongWidth）
rtv_numberSize    | 刻度数值字体大小 | 14sp
rtv_numberColor    | 刻度数值颜色 | #888888
