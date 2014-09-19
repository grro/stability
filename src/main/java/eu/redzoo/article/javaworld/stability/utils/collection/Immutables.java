/*
 * Copyright (c) 2014, Gregor Roth, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
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