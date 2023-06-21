package com.hgm.flowlayout.widget

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hgm.flowlayout.R
import com.hgm.flowlayout.utils.SizeUtil.Companion.dip2px

/**
 * @author：  HGM
 * @date：  2023-06-19 10:30
 * 使用自定义ViewGroup实现流式布局
 */
class FlowLayout : ViewGroup {
      //定义默认值
      companion object {
            const val DEFAULT_LINE = -1
            var DEFAULT_HORIZONTAL_PADDING = dip2px(5f)
            var DEFAULT_VERTICAL_PADDING = dip2px(5f)
            var DEFAULT_BORDER_RADIUS = dip2px(5f)
            const val DEFAULT_TEXT_MAX_LENGTH = -1
            const val TAG = "FlowLayout"
      }


      //定义属性
      private var maxLines = DEFAULT_LINE
      private var itemHorizontalPadding = DEFAULT_HORIZONTAL_PADDING
      private var itemVerticalPadding = DEFAULT_VERTICAL_PADDING
      private var textMaxLength = DEFAULT_TEXT_MAX_LENGTH
      private var textColor = 0
      private var borderColor = 0
      private var borderRadius = DEFAULT_BORDER_RADIUS

      //接收外部的内容数据
      private lateinit var list: ArrayList<String>
      private var lines: ArrayList<ArrayList<View>> = ArrayList()

      constructor(context: Context) : this(
            context, null
      )

      constructor(context: Context, attrs: AttributeSet?) : this(
            context, attrs, 0
      )

      //统一入口后，用this来调用，确保它进入第三个方法
      constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context, attrs, defStyleAttr
      ) {
            //获取属性
            initAttrs(context, attrs)
      }


      /**
       * 初始化属性
       */
      private fun initAttrs(context: Context, attrs: AttributeSet?) {
            //获取对象属性
            val a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
            //最大行数
            maxLines = a.getInt(R.styleable.FlowLayout_maxLine, DEFAULT_LINE)
            if (maxLines != -1 && maxLines < 1) {
                  //如果小于1且不是特殊-1，则抛出异常
                  throw IllegalArgumentException("FlowLayout_maxLine Can Not Less Than 1")
            }
            //水平方向边距
            itemHorizontalPadding = a.getDimension(
                  R.styleable.FlowLayout_itemHorizontalPadding,
                  DEFAULT_HORIZONTAL_PADDING.toFloat()
            ).toInt()
            //垂直方向边距
            itemVerticalPadding = a.getDimension(
                  R.styleable.FlowLayout_itemHorizontalPadding,
                  DEFAULT_VERTICAL_PADDING.toFloat()
            ).toInt()
            //文本最大长度
            textMaxLength = a.getInt(R.styleable.FlowLayout_textMaxLength, DEFAULT_TEXT_MAX_LENGTH)
            if (textMaxLength != DEFAULT_TEXT_MAX_LENGTH && textMaxLength < 0) {
                  throw IllegalArgumentException("FlowLayout_textMaxLength Can Not Less Than 0")
            }
            //文本颜色
            textColor = a.getColor(
                  R.styleable.FlowLayout_textColor,
                  resources.getColor(R.color.textColor)
            )
            //边框颜色
            borderColor = a.getColor(
                  R.styleable.FlowLayout_borderColor,
                  resources.getColor(R.color.textColor)
            )
            //边框圆角
            borderRadius =
                  a.getDimension(
                        R.styleable.FlowLayout_borderRadius,
                        DEFAULT_BORDER_RADIUS.toFloat()
                  ).toInt()
            a.recycle()
      }


      /**
       * 通过暴露方法，设置内容数据
       *
       * 设置内容的方法：
       *    1.使用adapter适配器
       *    2.手动添加（不方便）
       *    3.暴露方法，内部实现
       */
      fun setData(data: ArrayList<String>) {
            //接收外部数据
            list = ArrayList()
            list.apply {
                  clear()
                  addAll(data)
            }
            //根据数据创建子View并添加进来
            setUpChildView()
      }


      /**
       * 设置子View
       */
      private fun setUpChildView() {
            //先清空原有的不然刷新数据会叠加
            removeAllViews()
            //添加子View
            list.forEach { content ->
                  val textView = LayoutInflater.from(context)
                        .inflate(R.layout.item_flow_layout_text, this, false) as TextView
                  textView.apply {
                        //todo：配置TextView属性 颜色，边距，文本.....
                        if (textMaxLength != DEFAULT_TEXT_MAX_LENGTH) {
                              filters =
                                    Array<InputFilter>(1) { InputFilter.LengthFilter(textMaxLength) }
                        }
                        text = content
                        setOnClickListener {
                              //todo:点击
                              onItemClickListener?.onItemClick(this, content)
                        }
                  }.let {
                        addView(it)
                  }
            }
      }


      /**
       * 布局
       */
      override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val firstChild = getChildAt(0)
            var currentLeft = itemHorizontalPadding + paddingLeft
            var currentRight = itemHorizontalPadding + paddingRight
            var currentTop = itemVerticalPadding + paddingTop
            var currentBottom = firstChild.measuredHeight + itemVerticalPadding + paddingBottom
            lines.forEach { line ->
                  line.forEach { view ->
                        //每一行
                        val width = view.measuredWidth
                        currentRight += width
                        //处理item过长的问题（边界）
                        if (currentRight > measuredWidth - itemHorizontalPadding) {
                              currentRight = measuredWidth - itemHorizontalPadding
                        }
                        view.layout(currentLeft, currentTop, currentRight, currentBottom)
                        currentLeft = currentRight + itemHorizontalPadding
                        currentRight += itemHorizontalPadding
                  }
                  //下一行开始，调整数据
                  currentLeft = itemHorizontalPadding + paddingLeft
                  currentRight = itemHorizontalPadding + paddingRight
                  currentBottom += firstChild.measuredHeight + itemVerticalPadding
                  currentTop += firstChild.measuredHeight + itemVerticalPadding
            }
      }

      /**
       * 测量
       * 这两个值来自父控件，包含《值》和《模式》
       * 模式：取决于它的父控件（3种模式，可以不遵守父级模式）
       * 值：
       */
      override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            //...
            val mode = MeasureSpec.getMode(widthMeasureSpec)
            val parentWidthSize = MeasureSpec.getSize(widthMeasureSpec)
            val parentHeightSize = MeasureSpec.getSize(heightMeasureSpec)

            /*《测量孩子》*/
            //判断孩子数量，有孩子才去测量
            if (childCount == 0) {
                  return
            }

            //每次测量先清空防止重复
            lines.clear()
            var line = ArrayList<View>()
            lines.add(line)

            //设置测量孩子的标准
            val childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidthSize, MeasureSpec.AT_MOST)
            val childHeightSpec = MeasureSpec.makeMeasureSpec(parentHeightSize, MeasureSpec.AT_MOST)
            for (i in 0 until childCount) {
                  val child = getChildAt(i)
                  if (child.visibility != VISIBLE) {
                        continue
                  }
                  //测量孩子
                  measureChild(child, childWidthSpec, childHeightSpec)
                  if (line.size == 0) {
                        //直接添加
                        line.add(child)
                  } else {
                        //判断是否可以添加当前行
                        val canBeAdd = checkChildCanBeAdd(line, child, parentWidthSize)
                        if (!canBeAdd) {
                              //根据用户设置的行数，判断是否超过最大值，超过则不再添加
                              if (maxLines != -1 && lines.size >= maxLines) {
                                    break
                              }
                              //创建下一行添加
                              line = ArrayList()
                              lines.add(line)
                        }
                        line.add(child)
                  }
            }
            //根据每一行的高计算整个view的高度（动态的）（宽度是铺满父控件）
            val child = getChildAt(0)
            val childHeight = child.measuredHeight
            val parentHeightTargetSize =
                  childHeight * lines.size + (lines.size + 1) * itemVerticalPadding + paddingTop + paddingBottom

            /*《测量自己》*/
            setMeasuredDimension(parentWidthSize, parentHeightTargetSize)
      }


      private fun checkChildCanBeAdd(
            line: ArrayList<View>,
            child: View,
            parentWidthSize: Int
      ): Boolean {
            //已有view的总宽度
            var totalWidth = itemHorizontalPadding + paddingLeft
            line.forEach { view ->
                  totalWidth += view.measuredWidth + itemHorizontalPadding
            }
            //当前view的宽度
            val measuredWidth = child.measuredWidth + itemHorizontalPadding
            //判断宽度是否够用，小于等于总长度则可以添加
            totalWidth += measuredWidth + paddingRight
            return totalWidth < parentWidthSize
      }


      interface OnItemClickListener {
            fun onItemClick(v: View, text: String)
      }

      private var onItemClickListener: OnItemClickListener? = null

      fun setOnItemClickListener(listener: OnItemClickListener) {
            onItemClickListener = listener
      }
}