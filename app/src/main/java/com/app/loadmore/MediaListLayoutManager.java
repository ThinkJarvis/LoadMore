package com.app.loadmore;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;




/**
 * File description
 * 媒体列表控件,用于音乐和有声
 * 这里item宽高必须设置精确值
 *
 * @Title MediaListLayoutManager
 * @Author junyanyang
 * @Email junyanyang@pateo.com.cn
 * @Date 2018/06/6 10:30
 * @Version :v1.0
 ***/
public class MediaListLayoutManager extends RecyclerView.LayoutManager {
    private final static int ITEMS_ADAPTER_CHANGE = 100;
    private final static int ITEMS_ADD = 200;
    private final static int ITEMS_REMOVE = 300;
    private final static int ITEMS_CHANGE = 400;
    private int mItemsUpdateMode = -1;
    private boolean mScrolled;
    protected int mOffsetX = 0; //水平偏移
    private int mLeftX = 0; //view 左边的位置
    protected int mItemCount = 5;     //view一屏默认显示的数量
    protected float mItemspace, mStartX;
    protected int mChildWidth = -1;
    protected int mChildHeight;
    private int mDirection;
    protected static int SCROLL_LEFT = 1; //向左滑动,此时新增的view添加在左边
    protected static int SCROLL_RIGHT = 2; //向右滑动,此时新增的view添加在右边
    private SparseArray<Rect> mItemRects = new SparseArray<>(); //缓存item的位置,优化一下性能.但如果item太多可能会消耗内存,先设置一个长度,超过这个值自己动态获取
    private SparseBooleanArray mAttachItems = new SparseBooleanArray();//记录没有被回收且显示在屏幕上的view,防止滑动时候反复添加
    private static String TAG = MediaListLayoutManager.class.getSimpleName();
    protected RecyclerView.Recycler mRecycle;
    protected OrientationHelper mOrientationHelper;
    private int mSelectPosition = -1; //如果当前有选中的,直接滚动
    private int defaultCenterPosition = 0; //默认第一个中心点
    private ValueAnimator mAnimation; //滚动动画
    protected RecyclerView.State mState;
    private float mCenterX;
    private float scaleRate = 0.5f; //缩放系数
    private boolean mNoMusicSelect = false; //没有音乐的时候前面不显示
    private Rect mCurrentRect;  //为做动效方便先保存当前容器rect区域
    protected float mRangeSpace; //动效区域开始，动效区域结束，相对于当前矩形也就是mCurrentRect左边的距离
    private int testInt = 1;
    private RecyclerView mRecyclerView;
    private LinearSnapHelper mSnapHelper;
    private ItemAnimTransformer mItemAnimTransformer;
    protected float mOffsetBorder;  //滑动的边界
    private boolean mScrolling = false; //滚动状态
    private ScrollCallBack mScrollCallBack; //滚动状态回调接口
    private int scrollState = SCROLL_RIGHT; //一开始只能往右滑
    protected float mItemLeftEdge;
    private int mCenterPosition;
    private int mLeftPosition;
    private int mRightPosition;
    private float mTempOffsetX = -1;
    private boolean isEdgeBeforeAdd;
    protected int lastItemCount = -1;//增加前的item数目，优化增量刷新时候闪烁问题
    private int mMaxItem;//最大展示动画的距离,超过这个距离不展示动画,防止距离太大卡顿
    protected int halfCount;
    protected float leftEdge = -1; //超过这个数字,最左边显示不完全那个item完全不显示;


    public void resetOffset(){
        mSelectPosition = 0;
        mOffsetX = 0;
    }

    public int getOffsetX() {
        return mOffsetX;
    }


    public boolean isScrolling() {
        return mScrolling;
    }

    public void setScrollCallBack(ScrollCallBack scrollCallBack) {
        this.mScrollCallBack = scrollCallBack;
    }


    public int getSelectPosition() {
        return mSelectPosition;
    }


    /**
     * 获取可见区域,即当前坐标
     *
     * @return
     */
    private boolean inDisplayRect(Rect rectItem) {
        mCurrentRect = new Rect(mOffsetX, 0, mOffsetX + getParentWidth(), getParentHeight());
        return Rect.intersects(mCurrentRect, rectItem);
    }

    /**
     * 需要传入间距
     *
     * @param space item的间距
     */
    public MediaListLayoutManager(int space) {
        mSnapHelper = new LinearSnapHelper();
        this.mItemspace = space;
    }


    /**
     * 获取Item的位置信息
     *
     * @param index item位置
     * @return item的Rect信息
     */
    private Rect getFrame(int index) {
        Rect frame = mItemRects.get(index);
        if (frame == null) {
            frame = new Rect();
            float offset = mStartX + mRangeSpace * index; //原始位置累加（即累计间隔距离）
            frame.set(Math.round(offset), 0, Math.round(offset + mChildWidth), 0 + mChildHeight);
        }

        return frame;
    }

    /**
     * 计算初始值
     */
    private void calculateInitParams(View firstView) {
        //计算测量布局的宽高
        mChildWidth = getDecoratedMeasuredWidth(firstView);
        mChildHeight = getDecoratedMeasuredHeight(firstView);
        int totalWidth = getParentWidth();
        //起点在中心item左边界
        mCenterX = totalWidth / 2.0f;
        mStartX = (totalWidth - mChildWidth) / 2.0f;
        //计算左边item的个数,以item中部为锚点,计算半边可以容纳多少item
        halfCount = (int) Math.ceil(mStartX / ((mChildWidth + mItemspace) * 1.0f));  //向上取整
        mItemCount = halfCount * 2 + 1;  //两边加上中间的
        //计算动效的范围,距离中心这个范围以内就触发动效
        mRangeSpace = mChildWidth + mItemspace;
        //计算滑动边界,item和边距总宽度减去一屏加上头尾的间距,因为第一个item间距是mStartX，需要减掉一个mItemspace
        mOffsetBorder = (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + 2 * mStartX - mItemspace;
        mCenterPosition = 0;
        mLeftPosition = 0;
        mRightPosition = halfCount;
        mItemLeftEdge = mStartX;
        leftEdge = mStartX - (mRangeSpace) * halfCount + mChildWidth;
    }

    /**
     * 自定义动画
     *
     * @param itemAnimTransformer
     */
    public void setItemAnimTransformer(ItemAnimTransformer itemAnimTransformer) {
        this.mItemAnimTransformer = itemAnimTransformer;
    }

    /**
     * 给子View创建一个默认的LayoutParams对象
     *
     * @return
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    private boolean isEdgeWhenAdd() {
        if (lastItemCount < getItemCount()) { //局部增加
            int curCenterPosition = (int) (mOffsetX / mRangeSpace);
            if (curCenterPosition + (mItemCount / 2) >= lastItemCount) {
                lastItemCount = getItemCount();
                return true;
            }
            lastItemCount = getItemCount();
            return false;
        }
        return false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.d("onItemsAdded", "onLayoutChildren" + getItemCount());
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }
        if (state != null && state.isPreLayout()) {
            return;
        }
        mRecycle = recycler;
        mState = state;
        Log.d("mRecycle", "mRecycle" + mRecycle);
//        if ((mItemsUpdateMode == ITEMS_ADD)) {
//            addItemView(recycler);
//            mItemsUpdateMode = -1;
//            return;
//        }
//
//        //防止重复刷新，item位置没变化就退出,但是如果屏幕显示不全的时候得刷新一下，不然空白区域不显示了。
//        if (state.getItemCount() != 0 && !state.didStructureChange() && mItemsUpdateMode != ITEMS_CHANGE) {
//            return;
//        }
//        if (mItemsUpdateMode == ITEMS_REMOVE) { //批量删除由子类处理
//            mItemsUpdateMode = -1;
//            return;
//        }
        isEdgeBeforeAdd = false;
        mItemRects.clear();
        mAttachItems.clear();

        if (mChildWidth == -1) {  //第一次先计算初始值
          View first = recycler.getViewForPosition(0);
        addView(first);
        measureChildWithMargins(first, 0, 0);
            calculateInitParams(first);
        }


//        float offset = mStartX; //初始边距
//        for (int i = 0; i < getItemCount() && i < 50; i++) {
//            Rect frame = mItemRects.get(i);
//            if (frame == null) {
//                frame = new Rect();
//            }
//            mAttachItems.put(i, false);
//            frame.set(Math.round(offset), 0, Math.round(offset + mChildWidth), mChildHeight);
//            mItemRects.put(i, frame);
//            offset = offset + mItemspace + mChildWidth; //计算下一个的间距
//        }
        detachAndScrapAttachedViews(recycler);
        //开始添加view
        layoutChildren(recycler, state, SCROLL_RIGHT);

    }

    protected void addItemView(RecyclerView.Recycler recycler) {
        lastItemCount = getItemCount();
    }

    /**
     * adapter变化的时候重置数据
     *
     * @param oldAdapter
     * @param newAdapter
     */
    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        Log.d(TAG,"onAdapterChanged");
        removeAllViews();
        mItemRects.clear();
        mAttachItems.clear();
        mOffsetX = 0;
        mSelectPosition = 0;
        mOffsetBorder = (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + 2 * mStartX - mItemspace;

    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsUpdated(recyclerView, positionStart, itemCount);
        mItemsUpdateMode = ITEMS_CHANGE;
    }

    /**
     * item变化时候重新计算滑动边界
     *
     * @param recyclerView
     */
    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        Log.d(TAG,"onItemsChanged");
        lastItemCount = getItemCount();
        mOffsetBorder = (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + 2 * mStartX - mItemspace;
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        mItemsUpdateMode = ITEMS_ADD;
        Log.d("onItemsAdded", "ItemCount" + getItemCount());
        mOffsetBorder = (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + 2 * mStartX - mItemspace;

    }


    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        lastItemCount = getItemCount();
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
        mItemsUpdateMode = ITEMS_REMOVE;
        if (positionStart == 0) {
            if (mOffsetX >= (itemCount * mRangeSpace)) {
                mOffsetX = (int) (mOffsetX - (itemCount * mRangeSpace));
            } else {
                mOffsetX = 0;
            }
            if (mSelectPosition >= itemCount) {
                mSelectPosition = mSelectPosition - itemCount;
            } else {
                mSelectPosition = 0;
            }
        }
        mOffsetBorder = (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + 2 * mStartX - mItemspace;
        Log.d(TAG, " onItemsRemoved--->" + "mOffsetBorder" + mOffsetBorder + "mOffsetX" + mOffsetX);
    }

    /**
     * 没音乐了就调这个，让列表回到初始状态
     */
    public void setNoMusicSelect() {
        this.mNoMusicSelect = true;
        layoutChildren(mRecycle, mState, SCROLL_RIGHT); //从没音乐到有音乐是向右滚动的
    }

    public void setSelectPosition(int selectPosition) {
        this.mSelectPosition = selectPosition;
    }

    private void removeItem(RecyclerView.Recycler recycler) {
        if (getChildCount() == 0)
            return;
        /**
         * 移除边界外的item
         */
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
//            Log.d(TAG, "removeItem---->" + "i" + i + getPosition(view) + "mLeftPosition" + mLeftPosition + "mRightPosition" + mRightPosition);
            if (getPosition(view) < mLeftPosition || getPosition(view) > mRightPosition) {
                removeAndRecycleView(view, recycler);
            }
        }
    }

    /**
     * 排布子以及回收view
     *
     * @param recycler
     * @param state
     * @param direction
     */
    protected void layoutChildren(RecyclerView.Recycler recycler,
                                  RecyclerView.State state, int direction) {
        mCenterPosition = (int) (mOffsetX / mRangeSpace); //移动一个mRangeSpace的距离就是一个item
        float centerOffset = mOffsetX % mRangeSpace; //如果刚好滚到item中心位置，这个值为0，多出来的都是往左偏移的。
        //以中心点为边界计算，这里是屏幕的边界
        float leftEdge = mStartX - centerOffset;  //中心item左边位置，就是中心点左边界减去偏移量
        float rightEdge = leftEdge + mChildWidth; //右边的位置

        int leftCount = (int) Math.ceil(leftEdge / mRangeSpace); //允许不完全显示，要向上取整
        mLeftPosition = Math.max(mCenterPosition - leftCount, 0);//不能小于0
//        int rightCount = (int) Math.ceil(rightEdge / mRangeSpace);
        int rightCount = mItemCount / 2;   //右边item是固定的数量
        mRightPosition = Math.min(getItemCount() - 1, mCenterPosition + rightCount); //不能大于总item
        mItemLeftEdge = leftEdge - (mCenterPosition - mLeftPosition) * mRangeSpace;  //左边间距就是中心item的左边距加上相差item数目*mRangeSpace

        //todo 指定位置的时候这里有bug,会走太多次循环还要做一个判断
        if (getChildCount() != 0) {
            View startView = getChildAt(0);
            View endView = getChildAt(getChildCount() - 1);
            int startP = getPosition(startView);
            int endP = getPosition(endView);
            if (mLeftPosition > endP || mRightPosition < startP) {
                removeAllViews();
                Log.d("onItemClick", "超出了" + "mLeftPosition" + mLeftPosition + "mRightPosition" + mRightPosition + "startP" + startP + "endP" + endP);
            }
        }
        removeItem(recycler);  //先把左边右边范围外的回收
        if (getChildCount() == 0) { //如果没有item，从mLeftPosition到mRightPosition添加
            for (int i = mLeftPosition; i <= mRightPosition; i++) {
                View child = recycler.getViewForPosition(i);
                measureChildWithMargins(child, 0, 0);
                addView(child);
                layoutDecorated(child,
                        (int) mItemLeftEdge,
                        0,
                        (int) mItemLeftEdge + mChildWidth,
                        mChildHeight);  //排布view
                mItemLeftEdge += mRangeSpace;
            }
        } else {
            //计算最左边的可见item，如果大于mLeftPosition，向左添加直到相等
            View firstView = getChildAt(0);
            int first = getPosition(firstView);
            if (first > mLeftPosition) {
                for (int i = first - 1; i >= mLeftPosition; i--) {
                    View child = recycler.getViewForPosition(i);
                    measureChildWithMargins(child, 0, 0);
                    addView(child, 0);
                }
            }
            //计算最右2边的可见item，如果小于mRightPosition，向右添加直到相等
            View lastView = getChildAt(getChildCount() - 1);
            int last = getPosition(lastView);
            if (last < mRightPosition) {
                for (int i = last + 1; i <= mRightPosition; i++) {
                    View child = recycler.getViewForPosition(i);
                    measureChildWithMargins(child, 0, 0);
                    addView(child);
                }
            }
            //重新排布item位置
            for (int i = 0; i < getChildCount(); i++) {
                layoutDecorated(getChildAt(i),
                        (int) mItemLeftEdge,
                        0,
                        (int) mItemLeftEdge + mChildWidth,
                        mChildHeight);
                mItemLeftEdge += mRangeSpace;
            }
        }
        if (mItemAnimTransformer == null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            mItemAnimTransformer.transformItem(this, child, calculateAnimFraction(child));
        }

        Log.d(TAG, "count" + getChildCount() + "mItemLeftEdge" + mItemLeftEdge + "leftCount" + mLeftPosition + "rightCount" + mRightPosition);

//        mDirection = direction;
//        if (state.isPreLayout()) return;
//        int position = 0;  //以此为基准寻找附近的view判断是否可见，可见就add进去
//
//        if(mSelectPosition == -1){
//            setSelectPosition(position);
//        }else{
//            position = mSelectPosition;
//        }
//        //先移除可见区域外的view
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            position = getPosition(child);
//            Rect rect = getFrame(position);
//            if (!inDisplayRect(rect)) {//Item没有在显示区域，就说明需要回收
//                removeAndRecycleView(child, recycler); //回收滑出屏幕的View
//                mAttachItems.delete(position);
//            } else { //Item还在显示区域内，更新滑动后Item的位置
//                layoutItem(child, rect); //更新Item位置
//                mAttachItems.put(position, true);
////                Log.d(TAG,"position:" + position + "在显示区域 --> 刷新位置");
//            }
//        }
////        int min = position - 50 >= 0? position - 50 : 0;
////        int max = position + 50 < getItemCount() ? position + 50 : getItemCount();
//
//
//        int min = position - mItemCount >= 0 ? position - mItemCount : 0;
//        int max = position + mItemCount < getItemCount() ? position + mItemCount : getItemCount();
//        for (int i = min; i < max; i++) {
//            Rect rect = getFrame(i);
//            if (inDisplayRect(rect) && !mAttachItems.get(i)) { //重新加载可见范围内的Item
//                View scrap = recycler.getViewForPosition(i);
//                measureChildWithMargins(scrap, 0, 0);
//                if (direction == SCROLL_LEFT) { //向左滚动，新增的Item需要添加在最前面
//                    addView(scrap, 0);
//                } else { //向右滚动，新增的item要添加在最后面
//                    addView(scrap);
//                }
//                layoutItem(scrap, rect); //将这个Item布局出来
//                mAttachItems.put(i, true);
//            }else{
//
//            }
//        }
//        if(mItemAnimTransformer == null){
//            return;
//        }
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            mItemAnimTransformer.transformItem(this,child,calculateAnimFraction(child));
//        }
//
//        Log.d(TAG,"itemChild-->"   + getItemCount() + "childCount->" + getChildCount() + "mSelectPosition" + mSelectPosition + "mOffsetX" + mOffsetX);
    }


    public void attach(RecyclerView recyclerView) {
        if (recyclerView == null) {
            throw new IllegalArgumentException("The attach RecycleView must not null!!");
        }
        mRecyclerView = recyclerView;
        recyclerView.setLayoutManager(this);
        mSnapHelper.attachToRecyclerView(recyclerView);
        this.mSelectPosition = 0;

    }


    /**
     * 绑定recyclerView
     *
     * @param recyclerView
     * @param selectedPosition
     */
    public void attach(RecyclerView recyclerView, int selectedPosition) {
        if (recyclerView == null) {
            throw new IllegalArgumentException("The attach RecycleView must not null!!");
        }
        attach(recyclerView);
        setSelectPosition(selectedPosition);

//        recyclerView.addOnScrollListener(mInnerScrollListener);
    }


    private void layoutItem(View child, Rect frame) {

        Log.d(TAG, "left" + (frame.left - mOffsetX) + "right" + (frame.right - mOffsetX) + "offsetX" + mOffsetX);

        layoutDecorated(child,
                frame.left - mOffsetX,
                frame.top,
                frame.right - mOffsetX,
                frame.bottom);  //排布view

//        scaleItem(child);
    }

    /**
     * 设置滑动的边界,为总长度减去第一屏的宽度,再加一个边距
     *
     * @return
     */
    private float getOffsetBorder() {
        return (getItemCount()) * (mItemspace + mChildWidth) - getParentWidth() + mItemspace;
    }

    /**
     * 滑动动效在这里做
     *
     * @param dx
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx != 0) {
            mScrolled = true;
        }
        if (dx + mOffsetX < 0) {  //左滑到达边界
//            mOffsetX += travel; //累计偏移量
            mOffsetX = 0;
            forceStopScroll();
        } else if (dx + mOffsetX > mOffsetBorder) {  //右滑达到边界
            mOffsetX = (int) mOffsetBorder;
            forceStopScroll();
        } else {
            mOffsetX += dx; //正常情况下累计偏移量
        }
        scrollState = dx > 0 ? SCROLL_RIGHT : SCROLL_LEFT;
        layoutChildren(recycler, state, scrollState);
        return dx;
    }

    //是否到了滑动边界
    public boolean isBorder(boolean left) {
        if (mOffsetX == 0 && left) {
            return true;
        }
        if (mOffsetX == (int) mOffsetBorder && !left) {
            return true;
        }

        return false;
    }

    /**
     * 可以水平滑动
     *
     * @return
     */
    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    /**
     * 是否在缩放范围
     *
     * @param dx
     * @return
     */
    private boolean inScaleRange(int dx) {
        return false;
    }

    /**
     * 获取布局空间宽度
     *
     * @return
     */
    private int getParentWidth() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    /**
     * 获取布局空间高度
     *
     * @return
     */
    private int getParentHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
//                mScrollCallBack.onScrollIdle();
                //滚动停止时,需要修正使得最近的item居中
//                afterScroll();
//                if(mScrollCallBack !=null&&mScrolled){
//                    mScrolled = false;
//                    mScrollCallBack.onScrollIdle();
//                }
                View view = mSnapHelper.findSnapView(this);
                if (view != null) {
                    int position = getPosition(view);
                    if (mSelectPosition != position) {
                        setSelectPosition(position);
                    }
                    if (mTempOffsetX == -1) {
                        mHandler.postDelayed(onScrollingStopRunnable, 500);
                    }
                } else {
                    mSelectPosition = (int) (mOffsetX / mRangeSpace);
                    Log.d(TAG, "fail-->offsetX" + mOffsetX + "mOffsetBorder" + mOffsetBorder + "mSelectPosition" + mSelectPosition);
                }
                mScrolling = false;
//                mTempOffsetX = mOffsetX;
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (mScrollCallBack != null && !mScrolling) {
                    mScrollCallBack.onScrollingStart();
                }
                mScrolling = true;
                //拖拽滚动时
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                mScrolling = true;
                //动画滚动时
                break;
        }
    }


    /**
     * @param child
     * @return
     */
    private float calculateAnimFraction(View child) {
        int p = getPosition(child);
        int childCenter = child.getLeft() + mChildWidth / 2 + mOffsetX;
        int screenCenter = mOffsetX + getParentWidth() / 2;
        int offset = Math.abs(screenCenter - childCenter);
//        Log.d(TAG,"calculateAnimFraction --->" + "offset:" + offset + "screenCenter:" + screenCenter +  "childCenter:" + childCenter + "position" + p );
        if (offset > mRangeSpace) {
            return 0;
        } else {
            float fraction = (mRangeSpace - offset) / mRangeSpace;
            return fraction;
        }
    }

//    /**
//     * 停止滚动后,最近的item自动居中,要根据左右方向来计算
//     */
//    private void afterScroll() {
//        int scrollPosition = (int) ((mOffsetX  + (getParentWidth()/2))*1.0/(mItemspace + mChildWidth));
//        //先判断是否在item里面
////        View view = findViewByPosition(scrollPosition);
////        if(view.getLeft() < mOffsetX && view.getRight() > mOffsetX){
////
////        }
//        if(mDirection == SCROLL_LEFT){
//
//        }else{
////            scrollPosition = scrollPosition -1;
//        }
//
//        Log.d(TAG, "scrollPosition:" + scrollPosition);
//        int newOffsetX = getOffsetXbyPosition(scrollPosition);
//        if(scrollPosition < 2 ||scrollPosition > (getItemCount() -2)){
//            return;
//        }
//        Log.d(TAG, "middleSpace:" + newOffsetX + "mOffsetX:" + mOffsetX);
//        startScroll(mOffsetX, newOffsetX);
//        mSelectPosition = scrollPosition;
//    }


    /**
     * 带动画滚动到某个位置
     *
     * @param position
     */
    public boolean smoothToScrollToPosition(int position) {
        if (position == mSelectPosition || position >= getItemCount()) {
            return false;
        }
//        if (Math.abs(position - mSelectPosition) > 50) {
//            scrollToPosition(position);
//            return;
//        }
        int destOffsetX = getOffsetXbyPosition(position);
        startScroll(mOffsetX, destOffsetX, position);
        Log.d(TAG, "scrollToPosition->" + " destOffsetX" + destOffsetX + "position" + position + " mRangeSpace" + mRangeSpace);
        setSelectPosition(position);
        return true;
    }

    /**
     * 不带动画滚动到某个位置
     *
     * @param position 居中的位置
     */
    public boolean scrollToDestPosition(final int position) {
        if (position == mSelectPosition || position >= getItemCount()) {
            return false;
        }

        mOffsetX = getOffsetXbyPosition(position);
        layoutChildren(mRecycle, mState, position > mSelectPosition ? SCROLL_RIGHT : SCROLL_LEFT);
        setSelectPosition(position);
        if (mScrollCallBack != null) {
            mScrollCallBack.onScrollingStop(position);
        }
        return true;
    }

    /**
     * 根据位置计算当前item要居中要滑动多少距离
     *
     * @param poistion
     * @return
     */
    private int getOffsetXbyPosition(int poistion) {

        int count = poistion - defaultCenterPosition;
        return (int) ((mChildWidth + mItemspace) * count);
    }


    /**
     * 滚动到指定X轴位置
     *
     * @param from X轴方向起始点的偏移量
     * @param to   X轴方向终点的偏移量
     */
    private void startScroll(int from, int to, final int position) {
        if (mAnimation != null && mAnimation.isRunning()) {
            mAnimation.cancel();
        }
        final int direction = from < to ? SCROLL_RIGHT : SCROLL_LEFT;
        mAnimation = ValueAnimator.ofFloat(from, to);
        mAnimation.setDuration(300);
        mAnimation.setInterpolator(new DecelerateInterpolator());
        mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffsetX = Math.round((float) animation.getAnimatedValue());
                layoutChildren(mRecycle, mState, direction); //修正位置
            }
        });
        mAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mScrolling = true;
                if (mScrollCallBack != null) {
                    mScrollCallBack.onScrollingStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mScrolling = false;
                if (mScrollCallBack != null) {
                    Log.d("MusicTabBaseFragment", "onAnimationEnd--- >onScrollingStop");
                    mScrollCallBack.onScrollingStop(position);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimation.start();
    }

    /**
     * 获得第一个可见的view
     *
     * @return
     */
    public int findFirstVisiblePosition() {
        if (getChildCount() > 0) {
            View view = getChildAt(0);
            return getPosition(view);
        }
        return -1;
    }

    /**
     * 获得最后一个可见的view
     *
     * @return
     */
    public int findLastVisiblePosition() {
        if (getChildCount() > 0) {
            View view = getChildAt(getChildCount() - 1);
            return getPosition(view);
        }
        return -1;
    }

    public void scrollToIdle() {
        scrollToPosition(mItemCount - 1);
    }


    Handler mHandler = new Handler();
    Runnable onScrollingStopRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTempOffsetX == mOffsetX) {
                mScrolling = false;
                if (mScrollCallBack != null) {
                    Log.d("MusicTabBaseFragment", "onScrollingStopRunnable --- >onScrollingStop");
                    mScrollCallBack.onScrollingStop(mSelectPosition);
                }
                mTempOffsetX = -1;
            } else {
                mTempOffsetX = mOffsetX;
                mHandler.postDelayed(onScrollingStopRunnable, 500);
            }
        }
    };


    /**
     * 禁止滚动
     */
    public void forceStopScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));

    }

    public void setOffsetX(int offsetX) {
        this.mOffsetX = offsetX;
    }

}
