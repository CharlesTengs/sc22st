<template>
  <div>
    <a-row>
      <a-col :span="18">
        <div id="main" style="width: 100%;height:300px;"></div>
        <div id="viewCountChart" style="width: 100%;height:300px;"></div>
      </a-col>

      <a-col :span="6">
        <a-row :gutter="16">
          <a-col :span="12" class="first-column">
            <!-- Total Reading Count -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Total Reading Count" :value="statistic.viewCount">
                  <template #suffix>
                    <UserOutlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>

            <!-- Total Likes Count -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Total Likes Count" :value="statistic.voteCount">
                  <template #suffix>
                    <like-outlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>

            <!-- Like Rate -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Like Rate" :value="statistic.voteCount / statistic.viewCount * 100"
                             :precision="2"
                             suffix="%"
                             :value-style="{ color: '#cf1322' }">
                  <template #suffix>
                    <like-outlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>

            <!-- Today's Reading -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Today's Reading" :value="statistic.todayViewCount">
                  <template #suffix>
                    <UserOutlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>
          </a-col>

          <!-- Second Column -->
          <a-col :span="12">
            <!-- Today's Likes -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Today's Likes" :value="statistic.todayVoteCount">
                  <template #suffix>
                    <like-outlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>

            <!-- Estimated Today's Reading -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Estimated Today's Reading"
                             :value="statistic.todayViewIncrease"
                             :value-style="{ color: '#0000ff' }">
                  <template #suffix>
                    <UserOutlined />
                  </template>
                </a-statistic>
              </a-card>
            </div>

            <!-- Estimated Today's Reading Growth -->
            <div style="margin-bottom: 10px;">
              <a-card>
                <a-statistic title="Estimated Today's Reading Growth"
                             :value="statistic.todayViewIncreaseRateAbs"
                             :precision="2"
                             suffix="%"
                             class="demo-class"
                             :value-style="statistic.todayViewIncreaseRate < 0 ? { color: '#3f8600' } : { color: '#cf1322' }">
                  <template #prefix>
                    <arrow-down-outlined v-if="statistic.todayViewIncreaseRate < 0"/>
                    <arrow-up-outlined v-if="statistic.todayViewIncreaseRate >= 0"/>
                  </template>
                </a-statistic>
              </a-card>
            </div>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>




<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';

declare let echarts: any;

export default defineComponent({
  name: 'the-welcome',
  setup() {
    const statistic = ref();
    statistic.value = {};

    const getStatistic = () => {
      axios.get('/ebook-snapshot/get-statistic').then((response) => {
        const data = response.data;
        if (data.success) {
          const statisticResp = data.content;
          statistic.value.viewCount = statisticResp[1].viewCount;
          statistic.value.voteCount = statisticResp[1].voteCount;
          statistic.value.todayViewCount = statisticResp[1].viewIncrease;
          statistic.value.todayVoteCount = statisticResp[1].voteIncrease;
          const now = new Date();
          const nowRate = (now.getHours() * 60 + now.getMinutes()) / (60 * 24);
          statistic.value.todayViewIncrease = parseInt(String(statisticResp[1].viewIncrease / nowRate));
          statistic.value.todayViewIncreaseRate = (statistic.value.todayViewIncrease - statisticResp[0].viewIncrease) / statisticResp[0].viewIncrease * 100;
          statistic.value.todayViewIncreaseRateAbs = Math.abs(statistic.value.todayViewIncreaseRate);
        }
      });
    };

    const init30DayEcharts = (list: any) => {
      const mainDom = document.getElementById('main-col');
      if (mainDom) {
        mainDom.innerHTML = '<div id="main" style="width: 100%;height:300px;"></div>';
      }
      const myChart = echarts.init(document.getElementById('main'));
      const xAxis = [];
      const seriesView = [];
      const seriesVote = [];
      for (let i = 0; i < list.length; i++) {
        const record = list[i];
        xAxis.push(record.date);
        seriesView.push(record.viewIncrease);
        seriesVote.push(record.voteIncrease);
      }
      const option = {
        title: {
          text: '30-day Trend Chart',
          textStyle: {
            color: '#333',
            fontSize: 18
          }
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0,0,0,0.7)',
          padding: [10, 15],
          textStyle: {
            color: '#fff'
          }
        },
        legend: {
          data: ['Daily Total Reading Count', 'Daily Total Likes Count'],
          textStyle: {
            color: '#666'
          }
        },
        grid: {
          left: '1%',
          right: '3%',
          bottom: '3%',
          containLabel: true
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxis,
          axisLine: {
            lineStyle: {
              color: '#333'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#333'
            }
          }
        },
        series: [
          {
            name: 'Daily Total Reading Count',
            type: 'line',
            data: seriesView,
            smooth: true,
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                  offset: 0,
                  color: '#83bff6'
                }, {
                  offset: 0.5,
                  color: '#188df0'
                }, {
                  offset: 1,
                  color: '#188df0'
                }]),
                shadowColor: 'rgba(0, 0, 0, 0.1)',
                shadowBlur: 10
              }
            }
          },
          {
            name: 'Daily Total Likes Count',
            type: 'line',
            data: seriesVote,
            smooth: true,
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                  offset: 0,
                  color: '#E52C3C'
                }, {
                  offset: 0.5,
                  color: '#E96A78'
                }, {
                  offset: 1,
                  color: '#E52C3C'
                }]),
                shadowColor: 'rgba(0, 0, 0, 0.1)',
                shadowBlur: 10
              }
            }
          }
        ]
      };
      myChart.setOption(option);
    };

    const initViewCountChart = (viewCountlist: any) => {

      const sortedBooks = viewCountlist.sort((a: any, b: any) => b.viewCount - a.viewCount);

      const topBooks = sortedBooks.slice(0, 10); // 获取前10本书，可以更改为其他数量
      const bookNames = topBooks.map((book: any) => book.name); //
      const bookViewCounts = topBooks.map((book: any) => book.viewCount);

      // init ECharts
      const chartDom = document.getElementById('viewCountChart');
      const myChart = echarts.init(chartDom);

      const option = {
        title: {
          text: 'Top 10 Ebooks by View Count'
        },
        tooltip: {},
        xAxis: {
          data: bookNames,
          axisLabel: {
            interval: 0,
            rotate: 45,
            formatter: function(name: string) {
              return name.length > 10 ? `${name.slice(0, 12)}...` : name;
            }
          }
        },
        yAxis: {},
        series: [{
          name: 'View Count',
          type: 'bar',
          data: bookViewCounts
        }]
      };

      myChart.setOption(option);
    };

    const get30DayStatistic = () => {
      axios.get('/ebook-snapshot/get-30-statistic').then((response) => {
        const data = response.data;
        if (data.success) {
          const statisticList = data.content;
          init30DayEcharts(statisticList);
        }
      });
    };

    const getViewCountStatistic = () => {
      axios.get('/ebook/list', {
        params: {
          size: 100,
        }
      }).then((response) => {
        const data = response.data;
        if (data.success) {
          const viewCountlist = data.content.list;
          initViewCountChart(viewCountlist);
        }
      });
    };

    onMounted(() => {
      getStatistic();
      get30DayStatistic();
      getViewCountStatistic();
    });

    return {
      statistic
    };
        }
    });
</script>



<style scoped>
  .tip {
    padding: 10px 5px;
    margin-bottom: 20px;
    border: 1px solid transparent;
    background: linear-gradient(white,white) padding-box,repeating-linear-gradient(-45deg, black 0, black 25%, white 0, white 50%) 0/.6em .6em;
    animation:ants 12s linear infinite;
  }
  .tip b{
    color: red;
  }

  .first-column {
    padding-left: 20px !important;
  }
</style>
