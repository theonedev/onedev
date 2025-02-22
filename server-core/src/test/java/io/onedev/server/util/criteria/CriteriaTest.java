package io.onedev.server.util.criteria;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import io.onedev.server.util.Pair;

public class CriteriaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void forManyValues() {
        var ranges = new HashSet<Pair<Long, Long>>();
        var discretes = new HashSet<Long>();
        Criteria.forManyValues(newArrayList(2L, 3L), newArrayList(1L, 2L, 3L, 4L), new RangesAndDiscretesCollector(ranges, discretes));
        assertEquals(newHashSet(), ranges);
        assertEquals(newHashSet(2L, 3L), discretes);

        ranges.clear();
        discretes.clear();
        Criteria.forManyValues(newArrayList(1L, 2L, 3L, 4L, 6L, 7L, 8L, 11L, 12L), newArrayList(1L, 2L, 3L, 4L, 6L, 7L, 8L, 10L, 11L, 12L), new RangesAndDiscretesCollector(ranges, discretes));
        assertEquals(newHashSet(new Pair<>(1L, 8L)), ranges);
        assertEquals(newHashSet(11L, 12L), discretes);
    }

    private static class RangesAndDiscretesCollector implements NumberCriteriaBuilder {
        final Set<Pair<Long, Long>> ranges;

        final Set<Long> discretes;

        RangesAndDiscretesCollector(Set<Pair<Long, Long>> ranges, Set<Long> discretes) {
            this.ranges = ranges;
            this.discretes = discretes;
        }

        @Override
        public void forRange(long min, long max) {
            ranges.add(new Pair<>(min, max));
        }

        @Override
        public void forDiscretes(Collection<Long> numbers) {
            discretes.addAll(numbers);
        }
    }
}