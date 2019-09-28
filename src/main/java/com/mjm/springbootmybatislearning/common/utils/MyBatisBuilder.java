package com.mjm.springbootmybatislearning.common.utils;

import org.apache.ibatis.io.Resources;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyBatisBuilder {

    public static void main(String[] args) {

        InputStream inputStream = null;

        try {
            inputStream = Resources.getResourceAsStream("mybatis-generator/generatorConfig.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = null;

        try {
            config = cp.parseConfiguration(inputStream);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);

            System.out.println("config" + config.toString());


        } catch (IOException | XMLParserException | InterruptedException | SQLException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
