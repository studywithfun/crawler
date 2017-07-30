package com.cnoize.word.crawler.service;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cnoize.word.crawler.model.ShanbeiWord;
import com.cnoize.word.crawler.model.ShangbeiExample;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by sixu on 17/6/21.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GrabServiceTest {
    @Autowired
    private GrabService grabService;

    @Test
    public void testGetExample() {
        final List<ShangbeiExample> result = this.grabService.getExample(760);

        assertThat(result.size(), is(3));
    }

    @Test
    public void testGetWord() {
        final Optional<ShanbeiWord> result = this.grabService.getWord("abandon");
        assertTrue(result.isPresent());

        final ShanbeiWord word = result.get();
        assertThat(word.getId(), is(760L));

        final Optional<byte[]> ukAudio = this.grabService.downloadAudio(word.getUkAudio());
        assertTrue(ukAudio.isPresent());
    }
}
