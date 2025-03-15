package cn.bugstack.mall.member.service.impl;

import cn.bugstack.common.utils.HttpUtils;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;
import cn.bugstack.mall.member.dao.MemberDao;
import cn.bugstack.mall.member.dao.MemberLevelDao;
import cn.bugstack.mall.member.entity.MemberEntity;
import cn.bugstack.mall.member.entity.MemberLevelEntity;
import cn.bugstack.mall.member.exception.PhoneExistException;
import cn.bugstack.mall.member.exception.UserNameExistException;
import cn.bugstack.mall.member.service.MemberService;
import cn.bugstack.mall.member.vo.MemberLoginVO;
import cn.bugstack.mall.member.vo.MemberRegisterVO;
import cn.bugstack.mall.member.vo.SocialUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    private final MemberLevelDao memberLevelDao;

    public MemberServiceImpl(final MemberLevelDao memberLevelDao) {
        this.memberLevelDao = memberLevelDao;
    }

    @Override
    public PageUtils queryPage(final Map<String, Object> params) {
        final IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(final MemberRegisterVO memberRegister) {

        // 查询会员等级
        final MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();

        final MemberEntity member = new MemberEntity();
        member.setLevelId(levelEntity.getId()); // 默认会员等级
        // 检查用户名和手机号是否唯一，使用异常机制
        checkPhoneUnique(memberRegister.getPhone());
        checkUsernameUnique(memberRegister.getUserName());

        member.setMobile(memberRegister.getPhone());
        member.setUsername(memberRegister.getUserName());
        member.setNickname(memberRegister.getUserName());
        // 密码进行盐值加密
        final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        member.setPassword(bCryptPasswordEncoder.encode(memberRegister.getPassword()));

        // todo：其他默认信息

        this.save(member);
    }

    @Override
    public void checkPhoneUnique(final String phone) throws PhoneExistException {
        final Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(final String username) throws UserNameExistException {
        final Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(final MemberLoginVO memberLoginVO) {
        final String loginAccount = memberLoginVO.getLoginAccount();
        final MemberEntity member = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if (Objects.isNull(member)) {
            return null;
        }
        if (new BCryptPasswordEncoder().matches(memberLoginVO.getPassword(), member.getPassword())) {
            return member;
        } else {
            return null;
        }
    }

    @SneakyThrows
    @Override
    public MemberEntity login(final SocialUser socialUser) throws Exception {
        final String uid = socialUser.getUid();
        // 1. 判断当前社交用户是否已经登录过系统
        final MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (!Objects.isNull(memberEntity)) {
            final MemberEntity member = new MemberEntity();
            member.setId(memberEntity.getId());
            member.setAccessToken(socialUser.getAccess_token());
            member.setExpiresIn(String.valueOf(socialUser.getExpires_in()));
            this.baseMapper.updateById(member);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(String.valueOf(socialUser.getExpires_in()));
            return memberEntity;
        } else {
            final MemberEntity member = new MemberEntity();

            try {
                // 2. 没有查到当前社交用户对应的记录，需要注册一个新用户
                // 3. 根据社交用户信息查询当前社交用户的社交账号信息
                final HashMap<String, String> queryMap = Maps.newHashMap();
                queryMap.put("access_token", socialUser.getAccess_token());
                queryMap.put("uid", uid);
                final HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "GET", new HashMap<>(), queryMap);
                if (response.getStatusLine().getStatusCode() == 200) {
                    final String json = EntityUtils.toString(response.getEntity());
                    final JSONObject jsonObject = JSON.parseObject(json);
                    final String nickName = jsonObject.getString("name");
                    final String gender = jsonObject.getString("gender");
                    // 。。。
                    member.setNickname(nickName);
                    member.setGender("m".equals(gender) ? 1 : 0);
                    // 。。。
                }
            } catch (final Exception e) {
                member.setSocialUid(uid);
                member.setAccessToken(socialUser.getAccess_token());
                member.setExpiresIn(String.valueOf(socialUser.getExpires_in()));
                this.baseMapper.insert(member);
                return member;
            }
        }
        return null;
    }

}