package io.github.fzdwx.http;

import cn.hutool.core.date.LocalDateTimeUtil;
import io.github.fzdwx.lambada.lang.UnixTime;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/25 11:11
 */
public class HttpClient {

    public static void main(String[] args) {


        System.out.println(System.currentTimeMillis());
        System.out.println(1648286074125L);

        System.out.println("=================");

        System.out.println(1648286317);
        System.out.println(System.currentTimeMillis() / 1000);
        System.out.println(UnixTime.unixTime());
        System.out.println(UnixTime.now().toLocalDateTime());
        System.out.println(LocalDateTimeUtil.of(UnixTime.unixTime()));
    }
}