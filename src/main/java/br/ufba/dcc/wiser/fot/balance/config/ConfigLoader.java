/*
 * The MIT License
 *
 * Copyright 2017 Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.ufba.dcc.wiser.fot.balance.config;

import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Configurations loader.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class ConfigLoader {

    /* Private static gson loader object */
    private static final Gson GSON_OBJ = new Gson();

    /* This class shouldn't be instatiated */
    private ConfigLoader() {
    }

    /**
     *
     * Load a list of configurations from a configuration file with an specified
     * type.
     *
     * @param <T> Type of return of this file.
     * @param config_file_url URL to this file in disk.
     * @param config_file_type Type of data for GSON.
     * @return A list of configurations
     * @throws File not found exception if te system can't retrieve the file.
     */
    public static <T> List<T> configLoader(String config_file_url) throws FileNotFoundException {
        /* Configuration File */
        InputStream input_stream_reader;

        /* Get the configuration file */
        input_stream_reader = ConfigLoader.class.getResourceAsStream(config_file_url);

        /* Check if this file is correct */
        if (input_stream_reader == null) {
            FoTBalanceUtils.errorMsg("Canno't load configuration file = " + config_file_url);
            throw new FileNotFoundException("Cannot find file => " + config_file_url);
        }

        /* Get a buffered reader */
        BufferedReader config_file_buffer = new BufferedReader(new InputStreamReader(input_stream_reader));

        FoTBalanceUtils.infoMsg("Loaded Configuration file - " + config_file_url);

        /* Get GSON object and convert this file to a list of T object and after that return the config file */
        Type config_file_type = new TypeToken<ArrayList<T>>() {
        }.getType();
        List<T> configurations_file = GSON_OBJ.fromJson(config_file_buffer, config_file_type);

        return configurations_file;
    }

}
