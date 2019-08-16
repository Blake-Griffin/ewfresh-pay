package com.ewfresh.pay.base;

import com.ewfresh.pay.configure.Configure;
import com.ewfresh.pay.response.PayResponse;

/**
 * @author: <a href="mailto:liujing@sunkfa.com">LiuJing</a>
 */
public interface PayService {

    PayService setConfigure(Configure Configure);

    PayResponse pay();

}
