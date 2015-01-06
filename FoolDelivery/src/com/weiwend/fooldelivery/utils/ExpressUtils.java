package com.weiwend.fooldelivery.utils;

public class ExpressUtils {
	
	//根据给出的快递单号，返回模糊匹配成功的快递公司列表
	public static String[] getExpressNoForRule(String number)
	{
		int length=number.length();
		
		if(length<14||length>18){
			
			if(length==8||length==9){   //长度为8或者9
				
				return new String[]{"德邦"};
				
			}else if(length==10){     //长度为10
				
				if(number.matches("^(23[1-7]|610|611|710).*")
						||number.matches("^(230|50[2-5]).*")
						||number.matches("^(37[3-6]|317|322|323|327|329|330|460|466|48[0-2]|489|803|856|860|869).*")){
					
					return new String[]{"国通","宅急送","圆通"};
					
				}else if(number.matches("^(119|12[0-9]|130).*")){
					
					return new String[]{"国通"};
					
				}else{
					
					return new String[]{"圆通"};
				}
			}else if(length==11){    //长度为11
				
				if(number.matches("^\\d.*")){
					
					return new String[]{"京东"};
					
				}else{
					
					return new String[]{"UPS"};
					
				}
			}else if(length==12){    //长度为12
				
				if(number.matches("^\\d{3}.*"))    //前三位为数字
				{
				
					if(number.matches("^(268|368|468|568|668|868|888|900).*")){
						
						return new String[]{"申通"};
						
					}else if(number.matches("^(358|518|618|7[1-3]8|751|76[1-3]|778).*")){
						
						return new String[]{"中通"};
						
					}else if(number.matches("^(0(?!00)|11[34678]|131|199|20[3-6]|302|575|59[14]|660|730|756|90[3-5]|966).*")){
						
						return new String[]{"顺丰"};
						
					}else if(number.matches("^(2[158]0|420).*")){
						
						return new String[]{"汇通"};
						
					}else if(number.matches("^(560|580|776).*")){
						
						return new String[]{"天天"};
						
					}else if(number.matches("^(300|340|370|710).*")){
						
						return new String[]{"全峰"};
						
					}else if(number.matches("^768.*")){
						
						return new String[]{"申通","中通"};
						
					}else if(number.matches("^220.*")){
						
						return new String[]{"申通","汇通"};
						
					}else if(number.matches("^(701|660|757).*")){
	
						return new String[]{"中通","顺丰"};
						
					}else if(number.matches("^350.*")){
	
						return new String[]{"汇通","全峰"};
						
					}else if(number.matches("^(310|510).*")){
	
						return new String[]{"顺丰","汇通"};
						
					}else if(number.matches("^(550|886|530).*")){
						
						return new String[]{"天天","顺丰","快捷"};
						
					}else if(number.matches("^688.*")){
						
						return new String[]{"申通","顺丰","中通"};
						
					}else if(number.matches("^000.*")){
						
						return new String[]{"京东"};
						
					}else if(number.matches("^370.*")){
						
						return new String[]{"顺丰","全峰"};
						
					}else if(number.matches("^500.*")){
						
						return new String[]{"速尔","汇通"};
						
					}else if(number.matches("^880.*")){
						
						return new String[]{"速尔","快捷"};
						
					}else if(number.matches("^9[89]0.*")){
						
						return new String[]{"快捷"};
						
					}else{
						
						return new String[]{"顺丰","天天","汇通"};
						
					}
				}else if(number.matches("^[a-zA-Z]{3}.*")){   //前三位为字母
					
					return new String[]{"圆通"};
					
				}else{

					return new String[]{"顺丰","天天","汇通"};
					
				}
			}else if(length==13){
				
				if(number.matches("^\\d{2}.*")){   //前两位为数字
					
					if(number.matches("^\\d{3}.*")){  //前三位为数字
						
						if(number.matches("^(1[2345679]0|2[02]0|310|5[02]0|660|8[08]0|900).*")){
							
							return new String[]{"韵达"};
							
						}else if(number.matches("^(1[02][1-9]|11[012456789]).*")){
							
							return new String[]{"EMS"};
							
						}else if(number.matches("^(99[0-9]).*")){
							
							return new String[]{"邮政"};
							
						}else if(number.matches("^[15]00.*")){
							
							return new String[]{"韵达","EMS"};
							
						}else if(number.matches("^113.*")){
							
							return new String[]{"EMS","如风达"};
							
						}else if(number.matches("^([3579]13).*")){
							
							return new String[]{"如风达","韵达","EMS"};
							
						}else if(number.matches("^901.*")){
							
							return new String[]{"京东"};
							
						}else{
							
							return new String[]{"韵达","EMS","邮政"};
							
						}
						
					}else{

						return new String[]{"韵达","EMS","邮政"};
						
					}
					
				}else if(number.matches("^.{11}[a-zA-Z]{2}.*")){

					return new String[]{"EMS"};
					
				}else{
					
					return new String[]{"邮政"};
					
				}
			}
		}else{
			
			if(number.matches("^(1Z|1z).*")){
				
				return new String[]{"UPS"};
				
			}else{
				
				return new String[]{"如风达"};
				
			}
		}
		
		return null;
		
	}
	
	//根据快递公司名称获取id
	public static String getExpressCid(String name)
	{
	    if (name.equals("DHL")){
	    	
	    }else if (name.equals("圆通")){
	    	return "5";
	    }else if (name.equals("中通")){
	    	return "4";
	    }else if (name.equals("申通")){
	    	return "3";
	    }else if (name.equals("韵达")){
	    	return "6";
	    }else if (name.equals("德邦")){
	    	return "10";
	    }else if (name.equals("EMS")){
	    	return "1";
	    }else if (name.equals("国通")){

	    }else if (name.equals("汇通")){
	    	return "8";
	    }else if (name.equals("汇强")){

	    }else if (name.equals("快捷")){

	    }else if (name.equals("龙邦")){

	    }else if (name.equals("全一")){

	    }else if (name.equals("全峰")){
	    	return "9";
	    }else if (name.equals("全日通")){

	    }else if (name.equals("如风达")){
	    	return "12";
	    }else if (name.equals("顺丰")){
	    	return "2";
	    }else if (name.equals("速尔")){

	    }else if (name.equals("天天")){
	    	return "7";
	    }else if (name.equals("宅急送")){
	    	return "11";
	    }else if (name.equals("邮政")){

	    }else if ((name.equals("fedexInter")) || (name.equals("fedex"))){

	    }else if (name.equals("京东")){

	    }else if (name.equals("联昊通")){

	    }else if (name.equals("能达")){

	    }else if (name.equals("UPS")){

	    }else if (name.equals("优速")){

	    }else if (name.equals("增益")){

	    }else if (name.equals("大洋")){

	    }else if (name.equals("万象")){

	    }else if (name.equals("赛澳递")){

	    }else if (name.equals("远长")){

	    }else if (name.equals("宽容")){

	    }
	    
	    return "-1";

	}

}
