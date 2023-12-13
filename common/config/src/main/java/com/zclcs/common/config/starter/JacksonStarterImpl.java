package com.zclcs.common.config.starter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.zclcs.common.config.deserializer.MillisOrLocalDateDeserializer;
import com.zclcs.common.config.deserializer.MillisOrLocalDateTimeDeserializer;
import com.zclcs.common.config.deserializer.MillisOrLocalTimeDeserializer;
import com.zclcs.common.core.constant.DatePattern;
import com.zclcs.common.core.service.StarterService;
import io.vertx.core.json.jackson.DatabindCodec;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author zhouc
 */
public class JacksonStarterImpl implements StarterService {

    @Override
    public void setUp() throws Exception {
        setUpMapper();
    }

    private void setUpMapper() {
        ObjectMapper mapper = DatabindCodec.mapper();
        // 可解析反斜杠引用的所有字符
        mapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        // 允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        mapper.setLocale(Locale.CHINA);
        mapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN, Locale.CHINA));
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        // 单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 忽略json字符串中不识别的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略无法转换的对象
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DatePattern.DATETIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new MillisOrLocalDateTimeDeserializer(DatePattern.DATETIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DatePattern.DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new MillisOrLocalDateDeserializer(DatePattern.DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DatePattern.TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new MillisOrLocalTimeDeserializer(DatePattern.TIME_FORMATTER));
        mapper.registerModule(javaTimeModule);
        mapper.findAndRegisterModules();
    }
}
