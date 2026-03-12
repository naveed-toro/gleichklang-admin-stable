package de.binaerebauten.gleichklang.core.model.media;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

public enum Footprint implements DefaultEnumI18N
{
	SMILING_AT_YOU("smiling_at_you"),
	I_LIKE_YOU_PRETTY_WELL("i_like_you_pretty_well"),
	WANT_TO_GET_TO_KNOW_YOU("want_to_get_to_know_you"),
	YOU_ARE_ON_MY_WAVELENGTH("you_are_on_my_wavelength"),
	GREETING_IN_BETWEEN("greeting_in_between"),
	SENDING_SUNSHINE_TO_YOU("sending_sunshine_to_you"),
	SWEET_DREAMS("sweet_dreams"),
	WISH_YOU_A_NICE_DAY("wish_you_a_nice_day"),
	THINKING_OF_YOU("thinking_of_you"),
	WANNA_DATE_AGAIN("wanna_date_again"),
	WANNA_CALL("wanna_call"),
	DO_YOU_WRITE_TO_ME_SOON("do_you_write_to_me_soon"),
	THE_DATE_WAS_SUPER("the_date_was_super"),
	I_HAVE_BEEN_DREAMING_OF_YOU("i_have_been_dreaming_of_you"),
	I_MISS_YOU("i_miss_you"),
	I_LIKE_YOU("i_like_you"),
	I_AM_IN_LOVE_WITH_YOU("i_am_in_love_with_you"),
	YOU_ARE_IN_MY_HEART("you_are_in_my_heart"),
	I_LOVE_YOU("i_love_you"),
	YOU_ARE_THE_LOVE_OF_MY_LIFE("you_are_the_love_of_my_life"),
	FOREVER_TOGETHER_WITH_YOU("forever_together_with_you"),
	I_ADMIRE_YOUR_CLEVERNESS("i_admire_your_cleverness"),
	YOU_MAKE_ME_HAPPY("you_make_me_happy"),
	WHY_DO_I_NOT_HEAR_OF_YOU("why_do_i_not_hear_of_you"),
	WHY_DO_YOU_NOT_ANSWER("why_do_you_not_answer"),
	LETS_GET_ALONG_AGAIN("lets_get_along_again"),
	YOU_HURT_ME("you_hurt_me"),
	DIDNT_WANT_TO_HURT_YOU("didnt_want_to_hurt_you"),
	CANNOT_FORGET_YOU("cannot_forget_you"),
	MY_HEART_IS_BROKEN("my_heart_is_broken"),
	IS_THERE_ANOTHER_CHANCE_FOR_US("is_there_another_chance_for_us"),
	I_HAVE_NO_INTEREST("i_have_no_interest"),
	PLEASE_STOP_BOTHERING_ME("please_stop_bothering_me");

	static final String ROOT_PATH = "img/footprint/";
	static final String FILE_ENDING = ".png";
	final String path;
	
	Footprint(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return ROOT_PATH + path + FILE_ENDING;
	}

	@Override
	public String toString()
	{
		return msg();
	}
}
