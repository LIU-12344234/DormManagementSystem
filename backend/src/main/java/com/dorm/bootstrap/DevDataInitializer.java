package com.dorm.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.DormBuilding;
import com.dorm.entity.DormRoom;
import com.dorm.entity.Bed;
import com.dorm.entity.Notification;
import com.dorm.entity.Student;
import com.dorm.entity.SysRole;
import com.dorm.entity.SysUser;
import com.dorm.mapper.BedMapper;
import com.dorm.mapper.DormBuildingMapper;
import com.dorm.mapper.DormRoomMapper;
import com.dorm.mapper.NotificationMapper;
import com.dorm.mapper.SysRoleMapper;
import com.dorm.mapper.SysUserMapper;
import com.dorm.mapper.SysUserRoleMapper;
import com.dorm.mapper.StudentMapper;
import com.dorm.mapper.RepairOrderMapper;
import com.dorm.entity.RepairOrder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DevDataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final StudentMapper studentMapper;
    private final DormBuildingMapper dormBuildingMapper;
    private final DormRoomMapper dormRoomMapper;
    private final BedMapper bedMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final NotificationMapper notificationMapper;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(SysUserMapper sysUserMapper,
                              SysRoleMapper sysRoleMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              StudentMapper studentMapper,
                              DormBuildingMapper dormBuildingMapper,
                              DormRoomMapper dormRoomMapper,
                              BedMapper bedMapper,
                              RepairOrderMapper repairOrderMapper,
                              NotificationMapper notificationMapper,
                              PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.studentMapper = studentMapper;
        this.dormBuildingMapper = dormBuildingMapper;
        this.dormRoomMapper = dormRoomMapper;
        this.bedMapper = bedMapper;
        this.repairOrderMapper = repairOrderMapper;
        this.notificationMapper = notificationMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        SysRole adminRole = ensureRole("ADMIN", "管理员", "系统管理员");
        SysRole dormRole = ensureRole("DORM_MANAGER", "宿管员", "宿舍管理员");
        SysRole workerRole = ensureRole("WORKER", "维修工", "维修工用户");
        SysRole studentRole = ensureRole("STUDENT", "学生", "学生用户");

        SysUser admin = ensureUser("admin", "系统管理员", "admin123");
        bindRole(admin.getId(), adminRole.getId());

        SysUser dormManager = ensureUser("dorm", "宿管员", "dorm123");
        bindRole(dormManager.getId(), dormRole.getId());

        SysUser worker = ensureUser("worker", "维修工", "worker123");
        bindRole(worker.getId(), workerRole.getId());

        SysUser studentUser = ensureUser("stu", "学生张三", "stu123");
        bindRole(studentUser.getId(), studentRole.getId());

        ensureStudent(studentUser.getId(), "20250001", "张三", "M", "计算机学院", "软件工程", "软工1班", "13800000000");

        seedDormData(dormManager.getId());
        seedNotice(admin.getId());
        seedRepair(worker.getId());
    }

    private SysRole ensureRole(String roleKey, String roleName, String desc) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, roleKey)
            .last("limit 1"));
        if (role != null) {
            return role;
        }
        SysRole r = new SysRole();
        r.setRoleKey(roleKey);
        r.setRoleName(roleName);
        r.setDescription(desc);
        r.setEnabled(1);
        sysRoleMapper.insert(r);
        return r;
    }

    private SysUser ensureUser(String username, String displayName, String rawPwd) {
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username)
            .last("limit 1"));
        if (u != null) {
            return u;
        }
        SysUser nu = new SysUser();
        nu.setUsername(username);
        nu.setDisplayName(displayName);
        nu.setEnabled(1);
        nu.setPasswordHash(passwordEncoder.encode(rawPwd));
        sysUserMapper.insert(nu);
        return nu;
    }

    private void bindRole(Long userId, Long roleId) {
        if (sysUserRoleMapper.exists(userId, roleId) == null) {
            sysUserRoleMapper.insertIgnore(userId, roleId);
        }
    }

    private void ensureStudent(Long userId, String studentNo, String name, String gender, String college, String major, String className, String phone) {
        Student s = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
            .eq(Student::getUserId, userId)
            .last("limit 1"));
        if (s != null) {
            return;
        }
        Student ns = new Student();
        ns.setUserId(userId);
        ns.setStudentNo(studentNo);
        ns.setName(name);
        ns.setGender(gender);
        ns.setCollege(college);
        ns.setMajor(major);
        ns.setClassName(className);
        ns.setPhone(phone);
        ns.setStatus("ACTIVE");
        studentMapper.insert(ns);
    }

    private void seedDormData(Long dormManagerUserId) {
        long buildingCount = dormBuildingMapper.selectCount(new LambdaQueryWrapper<>());
        if (buildingCount > 0) {
            return;
        }

        DormBuilding b = new DormBuilding();
        b.setBuildingCode("B1");
        b.setBuildingName("一号宿舍楼");
        b.setAddress("校区A");
        b.setManagerUserId(dormManagerUserId);
        b.setEnabled(1);
        dormBuildingMapper.insert(b);

        DormRoom r = new DormRoom();
        r.setBuildingId(b.getId());
        r.setFloorNo(1);
        r.setRoomNo("101");
        r.setRoomType("NORMAL");
        r.setCapacity(4);
        r.setGenderLimit("M");
        r.setEnabled(1);
        dormRoomMapper.insert(r);

        for (int i = 1; i <= 4; i++) {
            Bed bed = new Bed();
            bed.setRoomId(r.getId());
            bed.setBedNo(String.valueOf(i));
            bed.setStatus("EMPTY");
            bedMapper.insert(bed);
        }
    }

    private void seedNotice(Long adminUserId) {
        long cnt = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>());
        if (cnt > 0) {
            return;
        }
        Notification n = new Notification();
        n.setTitle("欢迎使用学生宿舍管理系统");
        n.setContent("本系统支持床位分配、报修管理、访客登记、晚归登记、统计报表等功能。");
        n.setTargetType("ALL");
        n.setPublisherUserId(adminUserId);
        notificationMapper.insert(n);
    }

    private void seedRepair(Long workerUserId) {
        long cnt = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>());
        if (cnt > 0) {
            return;
        }

        Student s = studentMapper.selectOne(new LambdaQueryWrapper<Student>().last("limit 1"));
        DormRoom room = dormRoomMapper.selectOne(new LambdaQueryWrapper<DormRoom>().last("limit 1"));
        if (s == null || room == null) {
            return;
        }

        RepairOrder o = new RepairOrder();
        o.setOrderNo("R" + System.currentTimeMillis());
        o.setStudentId(s.getId());
        o.setRoomId(room.getId());
        o.setTitle("灯坏了");
        o.setDescription("宿舍灯不亮，疑似灯管坏了");
        o.setStatus("ASSIGNED");
        o.setPriority("NORMAL");
        o.setAssignedWorkerUserId(workerUserId);
        o.setAssignedAt(java.time.LocalDateTime.now());
        repairOrderMapper.insert(o);
    }
}
