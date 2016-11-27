/*
 * Copyright 2016 Ralic Lo<raliclo@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.raliclo.HackerRank_Java.Introduction;/**
 * Created by raliclo on 7/18/16.
 * Project Name : TestNG-1
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class JavaDatatypes {

    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT. Your class should be named Solution. */
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> x = reader.lines().collect(Collectors.toCollection(ArrayList::new));
//        System.out.println(x.size());
        x.remove(0);
        x.forEach((m) -> fitness(m));
    }

    public static void fitness(String m) {
        ArrayList<String> reply = new ArrayList<>();
        try {
            Long x = Long.parseLong(m);
            reply.add(x + " can be fitted in:");
            if (x >= Math.pow(-2, 7) && x <= (Math.pow(2, 7) - 1))
                reply.add("* byte");
            if (x >= Math.pow(-2, 15) && x <= (Math.pow(2, 15) - 1))
                reply.add("* short");
            if (x >= Math.pow(-2, 31) && x <= (Math.pow(2, 31) - 1))
                reply.add("* int");
            if (x >= Math.pow(-2, 63) && x <= (Math.pow(2, 63) - 1))
                reply.add("* long");
        } catch (Exception ex) {
            reply.add(m + " can't be fitted anywhere.");
        }
        reply.forEach(System.out::println);
    }
}
