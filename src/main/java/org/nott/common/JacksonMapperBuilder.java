package org.nott.common;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Configuration
public class JacksonMapperBuilder {

    /**
     * Jackson long类型转换为String
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        Jackson2ObjectMapperBuilderCustomizer cunstomizer = new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
                jacksonObjectMapperBuilder.serializerByType(Long.class, ToStringSerializer.instance);
            }
        };

        return cunstomizer;
    }

}
