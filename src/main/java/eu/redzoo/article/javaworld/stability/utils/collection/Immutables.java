/*
 * Copyright (c) 2014 Gregor Roth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.redzoo.article.javaworld.stability.utils.collection;

import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;



public class Immutables {

    public static <T> Collector<T, ?, ImmutableList<T>> toList() {
        Collector<T, ?, List<T>> collector = Collectors.toList();
        return Collectors.collectingAndThen(collector, ImmutableList::copyOf);
    }
    
    public static <T> Collector<T, ?, ImmutableSet<T>> toSet() {
        Collector<T, ?, List<T>> collector = Collectors.toList();
        return Collectors.collectingAndThen(collector, ImmutableSet::copyOf);
    }
        
    
    public static <T, K, U> Collector<T, ?, ImmutableMap<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                                    Function<? super T, ? extends U> valueMapper) {
        Collector<T, ?, Map<K,U>> collector = Collectors.toMap(keyMapper, valueMapper);
        return Collectors.collectingAndThen(collector, ImmutableMap::copyOf);
    }
    
    public static <F, T> ImmutableSet<T> transform(Set<F> fromList, Function<? super F, ? extends T> function) {   
        return fromList.stream().map(function).collect(toSet());
    }
    
    public static <F, T> ImmutableList<T> transform(List<F> fromList, Function<? super F, ? extends T> function) {   
        return fromList.stream().map(function).collect(toList());
    }
    
    public static <K, V1, V2> ImmutableMap<K, V2> transformValues(Map<K, V1> fromMap, Function<? super V1, V2> function) {
        Collector<Entry<K, V1>, ?, ImmutableMap<K, V2>> collector = toMap(Entry::getKey, entry -> function.apply(entry.getValue()));
        return fromMap.entrySet().stream().collect(collector);    
    }

    public static <K1, K2, V> ImmutableMap<K2, V> transformKeys(Map<K1, V> fromMap, Function<? super K1, K2> function) {
        Collector<Entry<K1, V>, ?, ImmutableMap<K2, V>> collector = toMap(entry -> function.apply(entry.getKey()), Entry::getValue);
        return fromMap.entrySet().stream().collect(collector);    
    }
    
    public static <K1, K2, V1, V2> ImmutableMap<K2, V2> transform(Map<K1, V1> fromMap, Function<? super K1, K2> keyFunction, Function<? super V1, V2> valueFunction) {
        Collector<Entry<K1, V1>, ?, ImmutableMap<K2, V2>> collector = toMap(entry -> keyFunction.apply(entry.getKey()), entry -> valueFunction.apply(entry.getValue()));
        return fromMap.entrySet().stream().collect(collector);    
    }
}