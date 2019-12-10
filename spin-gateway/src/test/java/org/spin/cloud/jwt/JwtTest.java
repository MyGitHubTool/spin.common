package org.spin.cloud.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.spin.core.security.Base64;
import org.spin.core.util.SerializeUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class JwtTest {
    private static String pri = "rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AAJFQ3VyAAJbQqzzF/gGCFTgAgAAeHAAAABhMF8CAQAwEAYHKoZIzj0CAQYFK4EEACMESDBGAgEBBEGqZmWeJ81mn4htpJK2pJtwkDtVmzz5o6M8e1kX15jQcT91nlRU9Qa+RMh4mMBgGayBbqkSVSa347Gm/QOUc3/nmXQABlBLQ1MjOH5yABlqYXZhLnNlY3VyaXR5LktleVJlcCRUeXBlAAAAAAAAAAASAAB4cgAOamF2YS5sYW5nLkVudW0AAAAAAAAAABIAAHhwdAAHUFJJVkFURQ==";
    private static String pub = "rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AAJFQ3VyAAJbQqzzF/gGCFTgAgAAeHAAAACeMIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQA84vkQGsHP3CcWTdI0HoVLory+K3EvE8cZ+weDcj5WiUB7VkZsgkgHUhIewcYG7Glq8rAlkNWHlqvIv3ZWpH+dN0AEW6puYgJWACr3Jd8X9NwpykWGxLagKzSODkaL77+d9OCJm2Zu3UGQ5EgN//XHDvu2W3VQmvJzcoFUANAx/8VCmh0AAVYLjUwOX5yABlqYXZhLnNlY3VyaXR5LktleVJlcCRUeXBlAAAAAAAAAAASAAB4cgAOamF2YS5sYW5nLkVudW0AAAAAAAAAABIAAHhwdAAGUFVCTElD";


    @Test
    void testJwt(String[] args) throws IOException {

        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.ES512);
        PrivateKey aPrivate = SerializeUtils.deserialize(Base64.decode(pri));
        PublicKey aPublic = SerializeUtils.deserialize(Base64.decode(pub));


        Date date = new Date();
        date.setTime(System.currentTimeMillis() + 3600_000L);
        String s = Jwts.builder()
            .setExpiration(date)
            .setAudience("1:aaa")
            .signWith(aPrivate)
            .compact();

        System.out.println(s);

        Claims body = Jwts.parser()
            .setSigningKey(aPublic)
            .parseClaimsJws(s)
            .getBody();
        System.out.println(body.getAudience());
    }

    @Test
    void testC() {
        System.out.println(tableSizeFor(200));
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;
}
