precision mediump float;			
varying vec4 v_Color;      	   								
 varying vec4 vPosition;
void main()                    		
{                    
	vec4 color;   
	color = v_Color;
	if(vPosition.y > float(0.99))
	{
		color = vec4(float(v_Color.x),float(v_Color.y),float(v_Color.z),float(0));
	}
	if(vPosition.y < float(-0.97))
	{
		color = vec4(float(v_Color.x),float(v_Color.y),float(v_Color.z),float(0));
	}       	
    gl_FragColor = color;                                  		
}