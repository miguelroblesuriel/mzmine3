/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.modules.visualization.frames;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/**
 * Wraps a {@link Canvas} so it can be resized automatically.
 * <a href="https://stackoverflow.com/a/31761362">source</a>
 */
public class CanvasPane extends Pane {

  private final Canvas canvas;

  public CanvasPane(Canvas canvas) {
    this.canvas = canvas;
    getChildren().add(canvas);
  }

  public CanvasPane(double width, double height) {
    canvas = new Canvas(width, height);
    getChildren().add(canvas);
  }

  public Canvas getCanvas() {
    return canvas;
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();
    final double x = snappedLeftInset();
    final double y = snappedTopInset();
    final double w = snapSizeX(getWidth()) - x - snappedRightInset();
    final double h = snapSizeY(getHeight()) - y - snappedBottomInset();
    canvas.setLayoutX(x);
    canvas.setLayoutY(y);
    canvas.setWidth(w);
    canvas.setHeight(h);
  }
}