package io.github.fzdwx.inf.http.core;

import io.netty.handler.codec.http.HttpHeaderValues;

/**
 * HTTP Content-Type.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 16:46
 * @see HttpHeaderValues
 */
public interface ContentType {

    /**
     * 文件上传编码，浏览器会把整个表单以控件为单位分割，并为每个部分加上Content-Disposition，并加上分割符(boundary)
     */
    String MULTIPART = "multipart/form-data; charset=utf-8";
    /**
     * text/html编码
     */
    String TEXT_HTML = "text/html; charset=utf-8";
    /**
     * text/plain编码
     */
    String TEXT_PLAIN = "text/plain; charset=utf-8";
    /**
     * Rest请求JSON编码
     */
    String JSON = "application/json; charset=utf-8";
    /**
     * Rest请求XML编码
     */
    String XML = "application/xml; charset=utf-8";
    /**
     * application/octet-stream编码
     */
    String OCTET_STREAM = "application/octet-stream; charset=utf-8";
    /**
     * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
     */
    String FORM_URLENCODED = "application/x-www-form-urlencoded; charset=utf-8";
}