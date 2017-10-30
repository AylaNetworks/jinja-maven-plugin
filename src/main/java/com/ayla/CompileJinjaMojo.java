package com.ayla;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

import com.hubspot.jinjava.Jinjava;

/**
 * Goal which renders a jinja file
 */
@Mojo(name = "renderjinja")
public class CompileJinjaMojo extends AbstractMojo {
  /**
   * Location of the file.
   */
  @Parameter(property = "templatefile", required = true)
  private File templateFile;

  @Parameter(property = "variablesfile", required = true)
  private File varFile;

  @Parameter(property = "outputfile", required = true)
  private File outputFile;

  @Parameter(property = "saltstack")
  private boolean saltstack = false;

  public void execute() throws MojoExecutionException {
    mergeOne(templateFile, varFile, outputFile);
    mergeMany();
  }

  /**
   * merges one template file with one values file
   * 
   * @param tmpl
   *          template file
   * @param sls
   *          values file
   * @param output
   *          merged file
   * @throws MojoExecutionException
   */
  private void mergeOne(File tmpl, File sls, File output) throws MojoExecutionException {
    try {
      System.out.println("jinjava merging: " + tmpl.getAbsolutePath() + " + " + sls.getAbsolutePath() + " --> "
          + output.getAbsolutePath());

      // Load the parameters
      Yaml yaml = new Yaml();
      @SuppressWarnings("unchecked")
      Map<String, Object> context = (Map<String, Object>) yaml.load(FileUtils.readFileToString(sls, (Charset) null));

      if (saltstack) {
        // Saltstack is expecting a dict named 'pillar'
        // Copying everything into this dict will allow devOps to just CnP the files
        // pillar['key'] = value

        // The normal java Map does not implement get(key, otherWiseUseThisDefault);
        //                MyCustomMap saltstackMap = new MyCustomMap();

        Map<String, Object> saltstackMap = new HashMap<String, Object>();
        saltstackMap.putAll(context);
        context.put("pillar", saltstackMap);
      }

      // Load template
      String template = FileUtils.readFileToString(tmpl, (Charset) null);

      // Render and save
      String rendered = new Jinjava().render(template, context);
      FileUtils.writeStringToFile(output, rendered, (Charset) null);
    } catch (IOException e) {
      // Print error and exit with -1
      throw new MojoExecutionException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * merges all template -> values files using implicit mapping: *.tmpl -> *.sls
   * 
   * @note: skips merging application.tmpl with application.sls
   * 
   * @throws MojoExecutionException
   */
  private void mergeMany() throws MojoExecutionException {

    try {
      // skip implicit coping if explicit copying executed before that didn't work 
      if (!(templateFile.exists() && templateFile.isFile() && outputFile.exists() && outputFile.isFile())) {
        return;
      }

      File oututputDir = outputFile.getParentFile();
      File[] templateFiles = oututputDir.listFiles();
      if (templateFiles == null || templateFiles.length < 1)
        return;

      Arrays.stream(templateFiles).filter(Objects::nonNull).forEach(

          tf -> {
            if (tf.isFile() && tf.getName().endsWith(".tmpl") && !tf.getName().equals("application.tmpl")) {
              File sls = new File(tf.getAbsolutePath().replace(".tmpl", ".sls"));
              if (sls.exists()) {
                try {
                  mergeOne(tf, sls,
                      new File(oututputDir.getAbsolutePath() + File.separator + tf.getName().replace(".tmpl", ".yml")));
                } catch (Exception e) {
                  e.printStackTrace();
                  System.exit(-1);
                }
              }
            }

          }

      );
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

  }

}

// class MyCustomMap extends HashMap {
//     public Object get(String key, String valueDefault) {
//         Object o = get(key);
//         if(null == o) {
//             Boolean b = Boolean.parseBoolean(valueDefault);
//             o = b;
//         }
//
//         return o;
//     }
//
//     public Object get(String key, boolean valueDefault) {
//         Object o = get(key);
//         if(null == o) {
//             Boolean b = valueDefault;
//             o = b;
//         }
//
//         return o;
//     }
//}