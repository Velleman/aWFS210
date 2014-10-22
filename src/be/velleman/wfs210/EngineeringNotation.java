package be.velleman.wfs210;

public class EngineeringNotation {

	private final static int PREFIX_OFFSET = 5;
	private final static String[] PREFIX_ARRAY = {"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

	public static String convert(double val, int dp)
	{
	   // If the value is zero, then simply return 0 with the correct number of dp
	   if (val == 0) return String.format("%." + dp + "f", 0.0);

	   // If the value is negative, make it positive so the log10 works
	   double posVal = (val<0) ? -val : val;
	   double log10 = Math.log10(posVal);

	   // Determine how many orders of 3 magnitudes the value is
	   int count = (int) Math.floor(log10/3);

	   // Calculate the index of the prefix symbol
	   int index = count + PREFIX_OFFSET;

	   // Scale the value into the range 1<=val<1000
	   val /= Math.pow(10, count * 3);

	   if (index >= 0 && index < PREFIX_ARRAY.length)
	   {
	      // If a prefix exists use it to create the correct string
	      return String.format("%." + dp + "f%s", val, PREFIX_ARRAY[index]);
	   }
	   else
	   {
	      // If no prefix exists just make a string of the form 000e000
	      return String.format("%." + dp + "fe%d", val, count * 3);
	   }
	}
}
