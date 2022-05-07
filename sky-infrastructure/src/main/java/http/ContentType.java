package http;

import java.nio.charset.Charset;

/**
 * http content type
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/05/07 18:02:55
 */
public enum ContentType {

    ALL("*/*"),

    /**
     * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
     */
    FORM_URLENCODED("application/x-www-form-urlencoded"),

    FORM_URLENCODED_UTF_8("application/x-www-form-urlencoded" + charset.UTF_8),

    /**
     * 文件上传编码，浏览器会把整个表单以控件为单位分割，并为每个部分加上Content-Disposition，并加上分割符(boundary)
     */
    MULTIPART("multipart/form-data"),

    MULTIPART_UTF_8("multipart/form-data" + charset.UTF_8),

    /**
     * Rest请求JSON编码
     */
    JSON("application/json"),

    JSON_UTF_8("application/json" + charset.UTF_8),

    /**
     * Rest请求XML编码
     */
    XML("application/xml"),

    XML_UTF_8("application/xml" + charset.UTF_8),

    /**
     * pdf
     */
    PDF("application/pdf"),

    /**
     * word 文档
     */
    DOC("application/msword"),

    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

    /**
     * 二进制数据
     */
    OCTET_STREAM("application/octet-stream"),

    /**
     * 表单数据
     */
    FROM_DATA("multipart/form-data"),

    /**
     * text/plain编码
     */
    TEXT_PLAIN("text/plain"),

    TEXT_PLAIN_UTF_8("text/plain" + charset.UTF_8),

    /**
     * Rest请求text/xml编码
     */
    TEXT_XML("text/xml"),

    TEXT_XML_UTF_8("text/xml" + charset.UTF_8),

    /**
     * text/html编码
     */
    TEXT_HTML("text/html"),

    TEXT_HTML_UTF_8("text/html" + charset.UTF_8),

    CSS("text/css"),

    CSV("text/csv"),

    IMAGE_JPEG("image/jpeg"),

    IMAGE_GIF("image/gif"),

    IMAGE_PNG("image/png"),

    IMAGE_BMP("image/bmp"),

    IMAGE_WEBP("image/webp"),

    IMAGE_SVG("image/svg+xml"),

    IMAGE_TIFF("image/tiff"),

    ICO("image/vnd.microsoft.icon"),

    AUDIO_MP4("audio/mp4"),

    MP3("audio/mpeg"),

    MPEG("video/mpeg"),

    /**
     * 富文本
     */
    RTF("application/rtf"),

    AAC("audio/aac"),

    AVI("video/x-msvideo"),

    STREAM_JSON("application/stream+json"),

    STREAM_JSON_UTF_8("application/stream+json" + charset.UTF_8),

    EVENT_STREAM("text/event-stream"),

    ;


    public final String value;

    /**
     * 构造
     *
     * @param value ContentType值
     */
    ContentType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    interface charset {

        String UTF_8 = "; charset=utf-8";
    }

    public String addEncode(Charset charset) {
        return value + "; charset=" + charset.name();
    }
}