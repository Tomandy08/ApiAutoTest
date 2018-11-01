package com.iiaccount.functioins;

import com.iiaccount.utils.RegisterLocationUtil;

import static com.iiaccount.utils.IdNoUtil.*;

/*
 *生成18位身份证号码函数
 */
public class IdNoFunction implements Function {
    @Override
    public String excute(String[] args) {
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(randomLocationCode(RegisterLocationUtil.registerLocation()));
        strBuffer.append(randomBirthday());
        strBuffer.append(randomCode());
        String eighteenth = verificationCode(strBuffer.toString());
        strBuffer.append(eighteenth);
        return strBuffer.toString();   //返回18位身份证号
    }

    @Override
    public String getReferenceKey() {
        return "idno";
    }
}
