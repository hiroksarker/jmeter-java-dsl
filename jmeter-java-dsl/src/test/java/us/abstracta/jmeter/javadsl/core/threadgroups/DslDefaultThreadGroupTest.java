package us.abstracta.jmeter.javadsl.core.threadgroups;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.threads.UltimateThreadGroup;
import kg.apc.jmeter.threads.UltimateThreadGroupGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.SampleErrorAction;

public class DslDefaultThreadGroupTest {

  private static final int DURATION1_SECONDS = 10;
  private static final int DURATION2_SECONDS = 15;
  private static final int DURATION3_SECONDS = 20;
  private static final int ITERATIONS = 10;
  private static final int THREAD_COUNT = 3;

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithThreadsAndIterations() {
    assertThat(new DslDefaultThreadGroup(null, THREAD_COUNT, ITERATIONS,
        Collections.emptyList()).buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, 0));
  }

  public TestElement buildSimpleThreadGroup(int threads, int iterations, int durationSecs,
      int rampUpSecs, int delaySecs) {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(threads);
    ret.setRampUp(rampUpSecs);
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (durationSecs != 0) {
      loopController.setLoops(-1);
      ret.setDuration(durationSecs);
    } else {
      loopController.setLoops(iterations);
    }
    if (delaySecs != 0) {
      ret.setDelay(delaySecs);
    }
    if (durationSecs != 0 || delaySecs != 0) {
      ret.setScheduler(true);
    }
    return ret;
  }

  public static TestElementAssert assertThatThreadGroup(TestElement actual) {
    return new TestElementAssert(actual);
  }

  public static class TestElementAssert extends
      AbstractAssert<TestElementAssert, Map<String, Object>> {

    public TestElementAssert(TestElement actual) {
      super(propsFrom(actual), TestElementAssert.class);
    }

    @Override
    public TestElementAssert isEqualTo(Object expected) {
      return super.isEqualTo(
          expected instanceof TestElement ? propsFrom((TestElement) expected) : expected);
    }

    private static Map<String, Object> propsFrom(TestElement elem) {
      Iterable<JMeterProperty> iterable = elem::propertyIterator;
      return StreamSupport.stream(iterable.spliterator(), false)
          .filter(p -> !("".equals(p.getName())))
          .collect(Collectors.toMap(JMeterProperty::getName, p -> {
            Object v = p.getObjectValue();
            return v instanceof TestElement ? propsFrom((TestElement) v) : v;
          }));
    }

  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldRampAndIterations() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .holdIterating(ITERATIONS)
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, DURATION2_SECONDS,
            DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRampAndIterations() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .holdIterating(ITERATIONS)
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldAndRampWithZeroDurationAndIterations() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRampWithZeroDurationAndIterations() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldAndRamp() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .buildThreadGroup())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION2_SECONDS, DURATION2_SECONDS,
                DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRamp() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .buildThreadGroup())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION1_SECONDS, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithThreadsAndDuration() {
    assertThatThreadGroup(
        new DslDefaultThreadGroup(null, THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS),
            Collections.emptyList()).buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION1_SECONDS, 0, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenHoldAndRampAndHoldDuration() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .holdFor(Duration.ofSeconds(DURATION3_SECONDS))
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION2_SECONDS + DURATION3_SECONDS,
            DURATION2_SECONDS, DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenRampAndHoldDuration() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .holdFor(Duration.ofSeconds(DURATION2_SECONDS))
        .buildThreadGroup())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION1_SECONDS + DURATION2_SECONDS,
                DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenRamp() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .buildThreadGroup())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION1_SECONDS, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenNoStages() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .buildThreadGroup())
        .isEqualTo(buildSimpleThreadGroup(1, 1, 0, 0, 0));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenIterationAfterIteration() {
    assertThrows(IllegalStateException.class, () -> new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .holdIterating(ITERATIONS));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenIterationInNonSimpleThreadGroup() {
    assertThrows(IllegalStateException.class, () -> new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .rampTo(0, Duration.ZERO)
        .holdIterating(ITERATIONS));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenRampAfterIteration() {
    assertThrows(IllegalStateException.class, () -> new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .rampTo(0, Duration.ZERO));
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionWhenRampWithNegativeThreads() {
    assertThrows(IllegalArgumentException.class, () -> new DslDefaultThreadGroup(null)
        .rampTo(-1, Duration.ZERO));
  }

  @Test
  public void shouldBuildUltimateThreadGroupWhenRampUpAndDown() {
    assertThatThreadGroup(new DslDefaultThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(0, Duration.ofSeconds(DURATION2_SECONDS))
        .buildThreadGroup())
        .isEqualTo(buildUltimateThreadGroup(
            new int[][]{{THREAD_COUNT, 0, DURATION1_SECONDS, 0, DURATION2_SECONDS}}));
  }

  private TestElement buildUltimateThreadGroup(int[][] schedsProps) {
    UltimateThreadGroup ret = new UltimateThreadGroup();
    PowerTableModel table = new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers,
        UltimateThreadGroupGui.columnClasses);
    for (int[] schedProps : schedsProps) {
      table.addRow(new Object[]{"" + schedProps[0], "" + schedProps[1], "" + schedProps[2],
          "" + schedProps[3], "" + schedProps[4]});
    }
    ret.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(table,
        UltimateThreadGroup.DATA_PROPERTY));
    LoopController loopController = new LoopController();
    loopController.setLoops(-1);
    loopController.setContinueForever(true);
    ret.setSamplerController(loopController);
    return ret;
  }

  @Test
  public void shouldBuildUltimateThreadGroupWhenComplexProfile() {
    Duration d = Duration.ofSeconds(10);
    DslDefaultThreadGroup threadGroup = new DslDefaultThreadGroup(null)
        .holdFor(d)
        .rampToAndHold(3, d, d)
        .rampToAndHold(2, d, d)
        .rampTo(5, d)
        .rampTo(3, d)
        .rampToAndHold(7, d, d)
        .rampToAndHold(5, d, d)
        .rampTo(1, d);
    assertThatThreadGroup(threadGroup.buildThreadGroup())
        .isEqualTo(buildUltimateThreadGroup(new int[][]{
            {1, 10, 4, 107, 0},
            {1, 14, 4, 101, 3},
            {1, 17, 4, 10, 10},
            {1, 50, 4, 62, 3},
            {2, 54, 7, 0, 10},
            {2, 70, 5, 35, 5},
            {2, 75, 5, 10, 10}
        }));
  }

  @Test
  public void shouldStopIteratingWhenThreadGroupWithStopThreadOnError() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(THREAD_COUNT, ITERATIONS)
            .sampleErrorAction(SampleErrorAction.STOP_THREAD)
            .children(
                httpSampler("http://myservice")
            )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(THREAD_COUNT);
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleIterationsThreadGroup() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              httpSampler("http://myhost")
          )
      );
    }

    public DslTestPlan simpleNamedIterationsThreadGroup() {
      return testPlan(
          threadGroup("myThreads", 1, 1,
              httpSampler("http://localhost"),
              httpSampler("http://myhost")
          )
      );
    }

    public DslTestPlan simpleDurationThreadGroup() {
      return testPlan(
          threadGroup(1, Duration.ofSeconds(1),
              httpSampler("http://localhost"),
              httpSampler("http://myhost")
          )
      );
    }

    public DslTestPlan simpleNamedDurationThreadGroup() {
      return testPlan(
          threadGroup("myThreads", 1, Duration.ofSeconds(1),
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan simpleThreadGroupWithRampAndIterations() {
      return testPlan(
          threadGroup()
              .rampTo(1, Duration.ofSeconds(2))
              .holdIterating(1)
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithRampAndDuration() {
      return testPlan(
          threadGroup()
              .rampToAndHold(1, Duration.ofSeconds(2), Duration.ofSeconds(1))
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithDelayAndIterations() {
      return testPlan(
          threadGroup()
              .holdFor(Duration.ofSeconds(1))
              .rampTo(1, Duration.ZERO)
              .holdIterating(1)
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithDelayAndDuration() {
      return testPlan(
          threadGroup()
              .holdFor(Duration.ofSeconds(1))
              .rampToAndHold(1, Duration.ZERO, Duration.ofSeconds(1))
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithRampDelayAndIterations() {
      return testPlan(
          threadGroup()
              .holdFor(Duration.ofSeconds(1))
              .rampTo(1, Duration.ofSeconds(2))
              .holdIterating(1)
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithRampDelayAndDuration() {
      return testPlan(
          threadGroup()
              .holdFor(Duration.ofSeconds(1))
              .rampToAndHold(1, Duration.ofSeconds(2), Duration.ofSeconds(1))
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleNamedThreadGroupWithRampAndDelay() {
      return testPlan(
          threadGroup("myThreads")
              .holdFor(Duration.ofSeconds(1))
              .rampTo(1, Duration.ofSeconds(2))
              .holdIterating(1)
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    /*
     this test uses as ramp duration difference of threads to avoid rounding issues in conversions
     of schedules.
     */
    public DslTestPlan complexThreadGroup() {
      return testPlan(
          threadGroup("myThreads")
              .holdFor(Duration.ofSeconds(1))
              .rampTo(3, Duration.ofSeconds(3))
              .rampTo(2, Duration.ofSeconds(1))
              .rampToAndHold(5, Duration.ofSeconds(3), Duration.ofSeconds(4))
              .rampTo(10, Duration.ofSeconds(5))
              .rampToAndHold(3, Duration.ofSeconds(7), Duration.ofSeconds(8))
              .rampTo(0, Duration.ofSeconds(3))
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

    public DslTestPlan simpleThreadGroupWithErrorAction() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              httpSampler("http://myhost")
          ).sampleErrorAction(SampleErrorAction.STOP_TEST)
      );
    }

    public DslTestPlan complexThreadGroupWithErrorAction() {
      return testPlan(
          threadGroup("myThreads")
              .rampTo(3, Duration.ofSeconds(3))
              .rampTo(0, Duration.ofSeconds(1))
              .sampleErrorAction(SampleErrorAction.STOP_TEST)
              .children(
                  httpSampler("http://localhost"),
                  httpSampler("http://myhost")
              )
      );
    }

  }

}
