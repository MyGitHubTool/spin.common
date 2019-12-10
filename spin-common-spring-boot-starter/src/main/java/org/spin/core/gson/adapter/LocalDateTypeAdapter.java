package org.spin.core.gson.adapter;

import org.spin.core.gson.DatePatternParser;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalDateTypeAdapter extends MatchableTypeAdapter<LocalDate> {
    private DateTimeFormatter formater;

    public LocalDateTypeAdapter(String datePattern) {
        this.formater = DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public LocalDate read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            return LocalDate.parse(tmp, DatePatternParser.getReadPattern(formater, field));
        }
    }

    @Override
    public void write(JsonWriter out, LocalDate value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(DatePatternParser.getWritePattern(formater, field).format(value));
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return LocalDate.class == type.getRawType();
    }
}
