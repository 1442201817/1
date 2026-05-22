package com.envmonitor.config;

import com.envmonitor.entity.AlertRule;
import com.envmonitor.entity.MonitorStation;
import com.envmonitor.entity.SysUser;
import com.envmonitor.repository.AlertRuleRepository;
import com.envmonitor.repository.EnvDataRepository;
import com.envmonitor.repository.StationRepository;
import com.envmonitor.repository.SysUserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final StationRepository stationRepository;
    private final SysUserRepository userRepository;
    private final AlertRuleRepository ruleRepository;

    public DataInitializer(StationRepository stationRepository,
                           SysUserRepository userRepository,
                           AlertRuleRepository ruleRepository) {
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
    }

    @PostConstruct
    public void init() {
        if (stationRepository.count() == 0) {
            initStations();
            System.out.println("✅ 监测站初始化完成（共16个站点）");
        } else {
            System.out.println("ℹ️  监测站数据已存在，跳过初始化（当前监测站数量: " + stationRepository.count() + "）");
        }

        if (userRepository.count() == 0) {
            initUsers();
            System.out.println("✅ 用户数据初始化完成");
        } else {
            System.out.println("ℹ️  用户数据已存在，跳过初始化（当前用户数量: " + userRepository.count() + "）");
        }

        if (ruleRepository.count() == 0) {
            initAlertRules();
            System.out.println("✅ 告警规则初始化完成");
        } else {
            System.out.println("ℹ️  告警规则已存在，跳过初始化（当前规则数量: " + ruleRepository.count() + "）");
        }
    }

    private void initStations() {
        String[][] stations = {
                {"黄浦区监测站",   "上海市黄浦区",         "31.22", "121.49"},
                {"徐汇区监测站",   "上海市徐汇区",         "31.19", "121.43"},
                {"长宁区监测站",   "上海市长宁区",         "31.22", "121.42"},
                {"静安区监测站",   "上海市静安区",         "31.23", "121.44"},
                {"普陀区监测站",   "上海市普陀区",         "31.25", "121.40"},
                {"虹口区监测站",   "上海市虹口区",         "31.28", "121.48"},
                {"杨浦区监测站",   "上海市杨浦区",         "31.26", "121.53"},
                {"闵行区监测站",   "上海市闵行区",         "31.11", "121.38"},
                {"宝山区监测站",   "上海市宝山区",         "31.41", "121.49"},
                {"嘉定区监测站",   "上海市嘉定区",         "31.38", "121.27"},
                {"浦东新区监测站", "上海市浦东新区",       "31.22", "121.54"},
                {"金山区监测站",   "上海市金山区",         "30.74", "121.34"},
                {"松江区监测站",   "上海市松江区",         "31.03", "121.23"},
                {"青浦区监测站",   "上海市青浦区",         "31.15", "121.12"},
                {"奉贤区监测站",   "上海市奉贤区",         "30.92", "121.47"},
                {"崇明区监测站",   "上海市崇明区",         "31.62", "121.40"},
        };
        for (String[] s : stations) {
            MonitorStation st = new MonitorStation();
            st.setName(s[0]);
            st.setLocation(s[1]);
            st.setLat(Double.parseDouble(s[2]));
            st.setLng(Double.parseDouble(s[3]));
            st.setStatus(MonitorStation.StationStatus.ONLINE);
            stationRepository.save(st);
        }
    }

    private void initUsers() {
        Object[][] users = {
                {"系统管理员", "管理员", "admin", "admin123", "", "", "正常", "系统默认管理员账户"},
                {"方天乐", "管理员", "fangtianle", "password123", "13800001111", "fangtianle@env.com", "正常", "负责整体项目协调与后端核心开发"},
                {"霍超然", "成员", "huochaoran", "password123", "13800002222", "huochaoran@env.com", "正常", "负责AI接口与数据模拟服务"},
                {"丁宇宁", "成员", "dingyuning", "password123", "13800003333", "dingyuning@env.com", "正常", "负责看板界面与图表开发"},
                {"姚佳文", "成员", "yaojiawen", "password123", "13800004444", "yaojiawen@env.com", "正常", "负责测试用例编写与文档整理"},
                {"李维", "管理员", "liwei", "password123", "13800005555", "liwei@env.com", "正常", "负责服务器运维与部署"},
                {"张晓敏", "成员", "zhangxiaomin", "password123", "13800006666", "zhangxm@env.com", "正常", "负责环境数据分析与报告输出"},
                {"王博", "成员", "wangbo", "password123", "13800007777", "wangbo@env.com", "禁用", "项目顾问"},
        };
        for (Object[] u : users) {
            SysUser user = new SysUser();
            user.setName((String) u[0]); 
            user.setRole((String) u[1]);
            user.setUsername((String) u[2]);
            user.setPassword((String) u[3]);
            user.setPhone((String) u[4]);
            user.setEmail((String) u[5]); 
            user.setStatus((String) u[6]);
            user.setRemark((String) u[7]);
            userRepository.save(user);
        }
    }

    private void initAlertRules() {
        Object[][] rules = {
                {"AQI",   ">",  100.0, "警告", true,  "AQI超过100触发警告"},
                {"AQI",   ">",  200.0, "严重", true,  "AQI超过200触发严重告警"},
                {"PM2.5", ">",   75.0, "警告", true,  "PM2.5超过日均标准75μg/m³"},
                {"PM2.5", ">",  150.0, "严重", true,  "PM2.5超过严重污染标准"},
                {"PM10",  ">",  150.0, "警告", true,  "PM10超标警告"},
                {"CO",    ">",  10.0, "警告", true,  "CO浓度超过10mg/m³"},
                {"温度",   ">",   40.0, "警告", false, "高温预警（夏季启用）"},
                {"湿度",   "<",   20.0, "警告", true,  "低湿度干燥预警"},
        };
        for (Object[] r : rules) {
            AlertRule rule = new AlertRule();
            rule.setMetricName((String) r[0]); rule.setOperator((String) r[1]);
            rule.setThreshold((Double) r[2]);  rule.setLevel((String) r[3]);
            rule.setEnabled((Boolean) r[4]);   rule.setDescription((String) r[5]);
            ruleRepository.save(rule);
        }
    }
}
