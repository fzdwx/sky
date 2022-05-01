package io.github.fzdwx.inf.http.core;

import io.netty.handler.codec.http.HttpHeaderValues;

/**
 * HTTP Content-Type.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
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

    String STREAM_JSON = "application/stream+json; charset=utf-8";
    /**
     * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
     */
    String FORM_URLENCODED = "application/x-www-form-urlencoded; charset=utf-8";

    String IMAGE_JPEG = "image/jpeg";
    String IMAGE_GIF = "image/gif";
    String IMAGE_PNG = "image/png";
    String IMAGE_BMP = "image/bmp";
    String IMAGE_WEBP = "image/webp";
    String IMAGE_SVG = "image/svg+xml";
    String IMAGE_TIFF = "image/tiff";
    String IMAGE_ICO = "image/x-icon";
    String IMAGE_ICNS = "image/x-icns";
    String IMAGE_ICON = "image/vnd.microsoft.icon";
    String IMAGE_ICON_BMP = "image/x-icon";
    String IMAGE_ICON_GIF = "image/gif";
    String IMAGE_ICON_PNG = "image/png";
    String IMAGE_ICON_SVG = "image/svg+xml";
    String IMAGE_ICON_TIFF = "image/tiff";
    String IMAGE_ICON_ICO = "image/x-icon";
    String IMAGE_ICON_ICNS = "image/x-icns";
    String IMAGE_ICON_ICON = "image/vnd.microsoft.icon";

    String AUDIO_MP3 = "audio/mpeg";
    String AUDIO_MP4 = "audio/mp4";
    String AUDIO_MPEG = "audio/mpeg";
    String AUDIO_OGG = "audio/ogg";
    String AUDIO_WEBM = "audio/webm";
    String AUDIO_WAV = "audio/wav";
    String AUDIO_WMA = "audio/x-ms-wma";
    String AUDIO_MID = "audio/midi";
    String AUDIO_MIDI = "audio/midi";
    String AUDIO_AAC = "audio/aac";
    String AUDIO_AIFF = "audio/aiff";
    String AUDIO_AU = "audio/basic";
    String AUDIO_SND = "audio/basic";
    String AUDIO_M3U = "audio/x-mpegurl";
    String AUDIO_M4A = "audio/mp4";
}