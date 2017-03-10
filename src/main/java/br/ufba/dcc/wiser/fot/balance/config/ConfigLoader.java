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

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class ConfigLoader {
    
    private ConfigLoader(){}
    
    public static ConfigFile ConfigLoader(String config_file_url){
        
        /* Configuration File */
        FileReader config_file_reader;
        
        try{
            /* Get the configuration file */
            config_file_reader = new FileReader(config_file_url);
        } catch(FileNotFoundException e){
            System.err.println("br.ufba.dcc.wiser.fot.balance.config.ConfigFile.<init>()");
            e.printStackTrace(new PrintStream(System.err));
            return null;
        }
        
        /* Get a buffered reader */
        BufferedReader config_file_buffer = new BufferedReader(config_file_reader);
        
        /* Get GSON object and convert config file to an object and after that return the config file */
        Gson gson = new Gson();
        ConfigFile config_file = gson.fromJson(config_file_buffer, ConfigFile.class);
        
        return config_file;
    }
    
}
