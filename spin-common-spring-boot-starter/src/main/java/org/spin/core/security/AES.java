package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.IntEvaluatable;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

/**
 * AES工具类
 * <p>使用强度超过 {@link KeyLength#WEAK} 的密钥需要JCE无限制权限策略文件(jdk 9以上不需要)</p>
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class AES extends ProviderDetector {
    private static final String ALGORITHM = "AES";

    private Mode mode = Mode.ECB;
    private Padding padding = Padding.PKCS5Padding;
    private byte[] iv;
    private SecretKey secretKey;
    private KeyLength keyLength;

    private Cipher enCipher;
    private Cipher deCipher;

    public enum KeyLength implements IntEvaluatable {
        WEAK(128),
        MEDIAM(192),
        STRONG(256);
        private int value;

        KeyLength(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return this.value;
        }
    }

    public enum Mode {
        ECB("ECB", false),
        CBC("CBC", true),
        PCBC("PCBC", true),
        CFB("CFB", true),
        OFB("OFB", true),
        CTR("CTR", true),
        GCM("GCM", true);
        private String value;
        private boolean needIv;

        Mode(String value, boolean needIv) {
            this.value = value;
            this.needIv = needIv;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum Padding {
        NoPadding("NoPadding"),
        ISO10126Padding("ISO10126Padding"),
        PKCS5Padding("PKCS5Padding"),
        PKCS7Padding("PKCS7Padding");
        private String value;

        Padding(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    private AES(Mode mode, Padding padding) {
        if (null != mode) {
            this.mode = mode;
        }
        if (null != padding) {
            this.padding = padding;
        }
        String cipherAlgorithm = ALGORITHM + "/" + this.mode.value + "/" + this.padding.value;
        if (this.mode.needIv) {
            iv = new byte[16];
            new Random().nextBytes(iv);
        }
        try {
            enCipher = Cipher.getInstance(cipherAlgorithm);
            deCipher = Cipher.getInstance(cipherAlgorithm);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "AES密码构造失败", e);
        }
    }

    public static SecretKey generateKey(String keySeed) {
        return generateKey(keySeed, KeyLength.WEAK);
    }

    public static SecretKey generateKey(String keySeed, KeyLength keySize) {
        KeyGenerator kg;
        SecureRandom secureRandom;
        try {
            kg = KeyGenerator.getInstance(ALGORITHM);
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SpinException(ErrorCode.KEY_FAIL, e);
        }
        kg.init(keySize.getValue(), secureRandom);
        //获取密匙对象
        SecretKey skey = kg.generateKey();
        //获取随机密匙
        byte[] raw = skey.getEncoded();
        return new SecretKeySpec(raw, "AES");
    }

    public static Key toKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static AES newInstance(Mode mode, Padding padding) {
        return new AES(mode, padding);
    }

    public static AES newInstance() {
        return new AES(null, null);
    }

    public synchronized AES withKey(SecretKey secretKey, KeyLength keyLength) {
        this.secretKey = Assert.notNull(secretKey, "密钥不能为空");
        this.keyLength = Assert.notNull(keyLength, "密钥强度不能为空");

        initCipher();
        return this;
    }

    public synchronized AES withKey(String secretKey) {
        keyLength = KeyLength.WEAK;
        this.secretKey = generateKey(Assert.notNull(secretKey, "密钥不能为空"), keyLength);

        initCipher();
        return this;
    }

    public synchronized AES withKey(String secretKey, KeyLength keyLength) {
        this.keyLength = Assert.notNull(keyLength, "密钥强度不能为空");
        this.secretKey = generateKey(Assert.notNull(secretKey, "密钥不能为空"), keyLength);

        initCipher();
        return this;
    }

    public synchronized AES withIv(byte[] iv) {
        if (mode.needIv) {
            Assert.isTrue(iv != null && iv.length >= 16, "初始化向量的长度不能小于16");
            System.arraycopy(iv, 0, this.iv, 0, 16);

            initCipher();
        }
        return this;
    }

    public String encrypt(String data) {
        return Base64.encode(encrypt(data.getBytes(StandardCharsets.UTF_8)));
    }

    public byte[] encrypt(byte[] data) {
        try {
            return enCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        }
    }

    public String decrypt(String data) {
        return decrypt(data, StandardCharsets.UTF_8);
    }

    public String decrypt(String data, Charset charset) {
        return new String(decrypt(Base64.decode(data)), charset);
    }

    public byte[] decrypt(byte[] data) {
        try {
            return deCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public Padding getPadding() {
        return padding;
    }

    private void initCipher() {
        try {
            if (mode.needIv) {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
                deCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            } else {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                deCipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "AES密码初始化失败", e);
        }
    }
}
