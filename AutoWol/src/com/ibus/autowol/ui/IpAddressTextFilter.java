package com.ibus.autowol.ui;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;

public class IpAddressTextFilter implements InputFilter
{

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) 
	{
	
		//Character.is
		
		//use the users input
		return null;
		
		
		 /*String textToCheck = destination.subSequence(0, destinationStart).  
		            toString() + source.subSequence(sourceStart, sourceEnd) +  
		            destination.subSequence(  
		            destinationEnd, destination.length()).toString();  
		    
		        Matcher matcher = mPattern.matcher(textToCheck);  
		    
		        // Entered text does not match the pattern  
		        if(!matcher.matches()){  
		     
		            // It does not match partially too  
		             if(!matcher.hitEnd()){  
		                 return "";  
		             }  
		     
		        }  
		    
		        return null;  */
		
		
		//TextView textView = (TextView)findViewById(R.id.mytextview01);
		/*Spannable WordtoSpan = new SpannableString("partial colored text");        
		WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		textView.setText(WordtoSpan);*/
	}

	
}
