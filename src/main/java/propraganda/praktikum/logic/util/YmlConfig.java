package propraganda.praktikum.logic.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;


@Getter
@Setter
@Slf4j
public class YmlConfig {

    private ObjectMapper mapper;

    public RoleConfig getUserTypes(final String path) throws IOException {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        return mapper.readValue(new File(path), RoleConfig.class);
    }



}
