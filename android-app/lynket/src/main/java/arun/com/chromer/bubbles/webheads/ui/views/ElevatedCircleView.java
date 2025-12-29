/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles.webheads.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;

/**
 * Circle view that draws bitmap shadow layers on pre L and system drop shadow on post L systems.
 */
public class ElevatedCircleView extends CircleView {

  public ElevatedCircleView(Context context) {
    this(context, null, 0);
  }

  public ElevatedCircleView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ElevatedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    setOutlineProvider(new ViewOutlineProvider() {
      @Override
      public void getOutline(View view, Outline outline) {
        int shapeSize = getMeasuredWidth();
        outline.setRoundRect(0, 0, shapeSize, shapeSize, shapeSize / 2f);
      }
    });
    setClipToOutline(true);
  }

  public void clearElevation() {
  }
}
