package com.demo;

import java.util.Random;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SnowApp extends Activity
{
	private static mainView		mView			= null;
	private static snow[]		snowFast		= null;
	private static snow[]		snowSlow		= null;
	private static fireball[]	fireball		= null;
	private int					mDisplayWidth	= 0;
	private int					mDisplayHeight	= 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initDisplay();
		mView = new mainView(this);
		setContentView(mView);
	}

	@Override
	protected void onDestroy()
	{
		mView.snowThdHigh.closeHandler();
		mView.snowThdLow.closeHandler();
		mView.fireballThd.closeHandler();
		snowFast = null;
		snowSlow = null;
		fireball = null;
		mView.mbmpBackground = null;
		mView.mbmpBackground0 = null;
		mView.mbmpBackground1 = null;
		mView.mbmpBackground2 = null;
		mView.mbmpBackground3 = null;
		mView.mbmpBackground4 = null;
		mView = null;
		super.onDestroy();
	}

	@Override
	public void onLowMemory()
	{

		// TODO Auto-generated method stub
		super.onLowMemory();
		System.gc();
	}

	private void initDisplay()
	{
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();

		mDisplayWidth = display.getWidth();
		mDisplayHeight = display.getHeight();
		display = null;
	}

	private class mainView extends View
	{
		private static final int	UPDATE_SNOW_HIGH	= 0;
		private static final int	UPDATE_SNOW_LOW		= 1;
		private static final int	UPDATE_FIREBALL		= 2;
		private static final int	UPDATE_BACKGROUND	= 3;
		private static final int	SHOW_DOWN			= 15;
		private static final int	SNOW_COUNT			= 50;
		private static final int	FIREBALL_COUNT		= 100;
		private static final int	SNOW_SPEED			= 100;
		private static final int	FIREBALL_SPEED		= 20;
		private int					mnClickCount		= 0;
		public Bitmap				mbmpBackground		= null;
		public Bitmap				mbmpBackground0		= null;
		public Bitmap				mbmpBackground1		= null;
		public Bitmap				mbmpBackground2		= null;
		public Bitmap				mbmpBackground3		= null;
		public Bitmap				mbmpBackground4		= null;
		private Paint				mPaintWhite;
		private Paint				mPaintBlue;
		private Paint				mPaintFireball;
		private int					mnBackGroundIndex	= 0;
		public HandleThreads		snowThdHigh			= new HandleThreads(Process.THREAD_PRIORITY_URGENT_AUDIO);
		public HandleThreads		snowThdLow			= new HandleThreads(Process.THREAD_PRIORITY_URGENT_AUDIO);
		public HandleThreads		fireballThd			= new HandleThreads(Process.THREAD_PRIORITY_URGENT_AUDIO);
		public Handler				mHandler			= null;
		public float				mnFireballX			= 0;
		public float				mnFireballY			= 0;

		public mainView(Context context)
		{
			super(context);
			setFocusable(true);
			initPaint();
			initBmp(context);
			snowFast = new snow[SNOW_COUNT];
			snowSlow = new snow[SNOW_COUNT];
			fireball = new fireball[FIREBALL_COUNT];

			for (int i = 0; i < SNOW_COUNT; i++)
			{
				snowFast[i] = new snow(0, 0);
				snowSlow[i] = new snow(0, 0);
			}

			for (int j = 0; j < FIREBALL_COUNT; j++)
			{
				fireball[j] = new fireball();
			}

			this.setOnTouchListener(TouchListener);

			/**
			 * main thread to update view
			 */
			mHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{

					// TODO Auto-generated method stub
					super.handleMessage(msg);

					if ((msg.what == UPDATE_SNOW_HIGH) || (msg.what == UPDATE_SNOW_LOW)
							|| (msg.what == UPDATE_FIREBALL))
					{
						mainView.this.invalidate();
					}
				}
			};
			snowThdHigh.startHandler();
			snowThdLow.startHandler();
			fireballThd.startHandler();
			snowThdHigh.getHandler().sendEmptyMessage(UPDATE_SNOW_HIGH);
			snowThdLow.getHandler().sendEmptyMessage(UPDATE_SNOW_LOW);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			canvas.drawColor(0xFFFFFFFF);
			canvas.drawBitmap(mbmpBackground, 0, 0, null);

			for (int i = 0; i < SNOW_COUNT; i++)
			{
				if (snowFast[i].exist)
				{
					canvas.drawCircle(snowFast[i].x, snowFast[i].y, 12, mPaintWhite);
				}

				if (snowSlow[i].exist)
				{
					canvas.drawCircle(snowSlow[i].x, snowSlow[i].y, 12, mPaintBlue);
				}
			}

			if (0 != fireballThd.mnFireballCount)
			{
				for (int j = 0; j < FIREBALL_COUNT; j++)
				{
					if (fireball[j].exist)
					{
						canvas.drawCircle(fireball[j].x, fireball[j].y, 5, mPaintFireball);
					}
				}
			}
		}

		private void initBmp(Context context)
		{
			Bitmap vBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bground0);
			Bitmap vB2 = Bitmap.createScaledBitmap(vBitmap, mDisplayWidth, mDisplayHeight, true);

			mbmpBackground0 = vB2;
			vBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bground1);
			vB2 = Bitmap.createScaledBitmap(vBitmap, mDisplayWidth, mDisplayHeight, true);
			mbmpBackground1 = vB2;
			vBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bground2);
			vB2 = Bitmap.createScaledBitmap(vBitmap, mDisplayWidth, mDisplayHeight, true);
			mbmpBackground2 = vB2;
			vBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bground3);
			vB2 = Bitmap.createScaledBitmap(vBitmap, mDisplayWidth, mDisplayHeight, true);
			mbmpBackground3 = vB2;
			vBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bground4);
			vB2 = Bitmap.createScaledBitmap(vBitmap, mDisplayWidth, mDisplayHeight, true);
			mbmpBackground4 = vB2;
			vBitmap = null;
			vB2 = null;
			mbmpBackground = mbmpBackground2;
		}

		private void initPaint()
		{
			mPaintWhite = new Paint();
			mPaintBlue = new Paint();
			mPaintFireball = new Paint();
			mPaintWhite.setAntiAlias(true);
			mPaintBlue.setAntiAlias(true);
			mPaintFireball.setAntiAlias(true);

			Shader linearGradient = new LinearGradient(0, 0, 24, 24, Color.WHITE, Color.GRAY, Shader.TileMode.MIRROR);

			mPaintWhite.setShader(linearGradient);
			linearGradient = new LinearGradient(0, 0, 24, 24, Color.WHITE, Color.BLUE, Shader.TileMode.MIRROR);
			mPaintBlue.setShader(linearGradient);
			linearGradient = new LinearGradient(0, 0, 10, 10, Color.RED, Color.YELLOW, Shader.TileMode.MIRROR);
			mPaintFireball.setShader(linearGradient);
			linearGradient = null;
		}

		OnTouchListener	TouchListener	= new OnTouchListener()
										{
											public boolean onTouch(View arg0, MotionEvent arg1)
											{

												// TODO Auto-generated method
												// stub
												int nAction = arg1.getAction();

												if (MotionEvent.ACTION_DOWN == nAction)
												{
													System.gc();
													mainView.this.mnFireballX = arg1.getX();
													mainView.this.mnFireballY = arg1.getY();
													mainView.this.fireballThd.mnFireballCount = 0;
													mainView.this.fireballThd.getHandler().sendEmptyMessage(
															mainView.UPDATE_FIREBALL);
													mnClickCount++;

													if (4 <= mnClickCount)
													{
														mnClickCount = 0;
														mainView.this.fireballThd.getHandler().sendEmptyMessage(
																mainView.UPDATE_BACKGROUND);
													}
												}

												return false;
											}
										};

		public void switchBackground()
		{
			switch (mnBackGroundIndex)
			{
				case 0:
					mbmpBackground = mbmpBackground0;

					break;

				case 1:
					mbmpBackground = mbmpBackground1;

					break;

				case 2:
					mbmpBackground = mbmpBackground2;

					break;

				case 3:
					mbmpBackground = mbmpBackground3;

					break;

				case 4:
					mbmpBackground = mbmpBackground4;

					break;

				default:
					mbmpBackground = mbmpBackground2;

					break;
			}

			mnBackGroundIndex++;

			if (4 < mnBackGroundIndex)
			{
				mnBackGroundIndex = 0;
			}
		}
	} // class mainView

	/**
	 *
	 * @author jugo snow ball class
	 */
	private class snow
	{
		public int		x		= 0;
		public int		y		= 0;
		public boolean	exist	= false;

		public snow(int nX, int nY)
		{
			x = nX;
			y = nY;
		}
	}

	/**
	 *
	 * @author Jugo fireball class
	 */
	private class fireball
	{
		public int		x		= 0;
		public int		y		= 0;
		public int		vx		= 0;
		public int		vy		= 0;
		public int		lasted	= 0;
		public boolean	exist	= false;

		public fireball()
		{
		}
	}

	/**
	 *
	 * @author jugo snow thread class
	 */
	private class HandleThreads
	{
		private static final String	TAG				= "SnowThreads";
		private Handler				mHandler		= null;
		private HandlerThread		mHandlerThread	= null;
		private Looper				mLooper			= null;
		private int					mnCount			= 0;
		public int					mnFireballCount	= 0;
		private Random				rand			= new Random();

		public HandleThreads(int nPriority)
		{
			mHandlerThread = new HandlerThread(TAG, nPriority);
		}

		public void startHandler()
		{
			if (null != mHandlerThread)
			{
				mHandlerThread.start();
				mLooper = mHandlerThread.getLooper();
				mHandler = new Handler(mLooper)
				{
					public void handleMessage(Message msg)
					{
						switch (msg.what)
						{
							case mainView.UPDATE_SNOW_HIGH:
								this.removeMessages(msg.what);
								setWhiteSnow();
								invalidDateView(msg.what, mainView.SNOW_SPEED);

								break;

							case mainView.UPDATE_SNOW_LOW:
								this.removeMessages(msg.what);
								setBlueSnow();
								invalidDateView(msg.what, mainView.SNOW_SPEED + 200);

								break;

							case mainView.UPDATE_FIREBALL:

								// this.removeMessages(msg.what);
								if (setFireball())
								{
									invalidDateView(msg.what, mainView.FIREBALL_SPEED);
								}

								break;

							case mainView.UPDATE_BACKGROUND:
								mView.switchBackground();

								break;
						}
					}
				};
			}
		}

		public void stopHandler()
		{
			if (null != mHandlerThread)
			{
				mHandlerThread.stop();
			}
		}

		public void closeHandler()
		{
			if (null != mHandlerThread)
			{
				mHandlerThread.quit();
				mHandlerThread = null;
			}
		}

		public Handler getHandler()
		{
			return mHandler;
		}

		public Looper getLooper()
		{
			return mLooper;
		}

		private void setWhiteSnow()
		{
			if (mnCount < mainView.SNOW_COUNT)
			{
				snowFast[mnCount].x = rand.nextInt() % mDisplayWidth;
				snowFast[mnCount].y = 0;
				snowFast[mnCount].exist = true;
				mnCount++;
			}

			for (int i = 0; i < mainView.SNOW_COUNT; i++)
			{
				if (snowFast[i].exist)
				{
					if (rand.nextInt() % 2 == 0)
					{
						snowFast[i].x += 3;
					}
					else
					{
						snowFast[i].x -= 3;
					}

					snowFast[i].y += mainView.SHOW_DOWN;

					if (snowFast[i].y > (mDisplayHeight - 48))
					{
						snowFast[i].x = rand.nextInt() % mDisplayWidth;
						snowFast[i].y = 0;
					}
				}
			}
		}

		private void setBlueSnow()
		{
			if (mnCount < mainView.SNOW_COUNT)
			{
				snowSlow[mnCount].x = rand.nextInt() % mDisplayWidth;
				snowSlow[mnCount].y = 0;
				snowSlow[mnCount].exist = true;
				mnCount++;
			}

			for (int i = 0; i < mainView.SNOW_COUNT; i++)
			{
				if (snowSlow[i].exist)
				{
					if (rand.nextInt() % 2 == 0)
					{
						snowSlow[i].x += 3;
					}
					else
					{
						snowSlow[i].x -= 3;
					}

					snowSlow[i].y += mainView.SHOW_DOWN;

					if (snowSlow[i].y > (mDisplayHeight - 48))
					{
						snowSlow[i].x = rand.nextInt() % mDisplayWidth;
						snowSlow[i].y = 0;
					}
				}
			}
		}

		private boolean setFireball()
		{
			if (initFireball())
			{
				return true;
			}

			for (int i = 0; i < mainView.FIREBALL_COUNT; i++)
			{
				if (fireball[i].exist)
				{
					fireball[i].x += fireball[i].vx;
					fireball[i].y += fireball[i].vy;
					fireball[i].lasted++;

					if ((fireball[i].x <= -10) || (fireball[i].x > mDisplayWidth) || (fireball[i].y <= -10)
							|| (fireball[i].y > mDisplayHeight) || (fireball[i].lasted > 50))
					{
						fireball[i].exist = false;
						mnFireballCount--;
					}
				}
			}

			if (0 == mnFireballCount)
			{
				return false;
			}

			return true;
		}

		private boolean initFireball()
		{
			boolean bInit = false;
			int nRemin = 7;

			if (0 == mnFireballCount)
			{
				for (int i = 0; i < mainView.FIREBALL_COUNT; i++)
				{
					fireball[i].x = (int) mView.mnFireballX;
					fireball[i].y = (int) mView.mnFireballY;
					fireball[i].lasted = 0;

					if (i % 2 == 0)
					{
						fireball[i].vx = -rand.nextInt() % nRemin;
						fireball[i].vy = -rand.nextInt() % nRemin;
					}

					if (i % 2 == 1)
					{
						fireball[i].vx = rand.nextInt() % nRemin;
						fireball[i].vy = rand.nextInt() % nRemin;
					}

					if (i % 4 == 2)
					{
						fireball[i].vx = -rand.nextInt() % nRemin;
						fireball[i].vy = rand.nextInt() % nRemin;
					}

					if (i % 4 == 3)
					{
						fireball[i].vx = rand.nextInt() % nRemin;
						fireball[i].vy = -rand.nextInt() % nRemin;
					}

					fireball[i].exist = true;
				}

				mnFireballCount = mainView.FIREBALL_COUNT;
				bInit = true;
			}

			return bInit;
		}

		private void invalidDateView(int nWhat, int nDuration)
		{
			if ((null == mView) || (null == mView.mHandler))
			{
				return;
			}

			mView.mHandler.sendEmptyMessage(nWhat);

			Message msg1 = new Message();

			msg1.what = nWhat;
			mHandler.sendMessageDelayed(msg1, nDuration);
			msg1 = null;
		}
	}
}
