/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package be.velleman.wfs210;

public class Constants
{
	public static final int BYTES_PER_FLOAT = 4;
	public static final float SAMPLES_PER_DIVISION = 50f;
	public final float DIVISIONS = 1;
	public final float TOTAL_SAMPLES = SAMPLES_PER_DIVISION * DIVISIONS;
	public static final float SAMPLE_HEIGHT = 255f;
	public static final int VIEWPORTVERTICALTOPOFFSET = 7;
	public static final int VIEWPORTVERTICALBOTTOMOFFSET = 6;
	public static final int VIEWPORTHORIZONTALLEFTOFFSET = 11;
	public static final int VIEWPORTHORIZONTALRIGHTOFFSET = 6;
	public static final String PREFS_NAME = "WFS210Preferences";

}
