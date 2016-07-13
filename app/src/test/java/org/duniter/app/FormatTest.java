package org.duniter.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import android.widget.TextView;


import static org.mockito.Mockito.*;

import org.duniter.app.object.TestMockContext;
import org.duniter.app.object.MockTextView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;


/**
 * Created by naivalf27 on 22/06/16.
 */
@RunWith(PowerMockRunner.class)
public class FormatTest {

    @Mock
    SharedPreferences mMockSharedPreferences;

    @Mock
    PreferenceManager mMockPreferencManager;

    @Mock
    Context mMockContext;

    @PrepareForTest({ PreferenceManager.class })
    @Test
    public void testInitUnit() throws Exception {
//        TestMockContext mockContext = new TestMockContext(mMockContext);
//        TextView textView = new MockTextView(mMockContext);
//
//        PowerMockito.mockStatic(PreferenceManager.class);
//        PowerMockito.when(PreferenceManager.getDefaultSharedPreferences(mockContext))
//                .thenReturn(mMockSharedPreferences);
//
//        when(mMockSharedPreferences.getString(eq(Application.UNIT), anyString()))
//                .thenReturn("0");
//        when(mMockSharedPreferences.getString(eq(Application.UNIT_DEFAULT), anyString()))
//                .thenReturn("1");
//        when(mMockContext.getString(eq(R.string.ud)))
//                .thenReturn("DU");
//
//        Format.initUnit(mockContext,textView,1000,0,10,100,0,true,"laCurrency");
//
//        Assert.assertTrue(textView.getText().toString().equals("1 000 LC"));
    }

    @Test
    public void testConvertBase() throws Exception {
        long result = Format.convertBase(16,0,1);
        Assert.assertTrue(result==1);

        result = Format.convertBase(16,1,0);
        Assert.assertTrue(result==160);

        result = Format.convertBase(16,0,2);
        Assert.assertTrue(result==0);

        result = Format.convertBase(16,1,1);
        Assert.assertTrue(result==16);
    }

    @Test
    public void testMinifyPubkey() throws Exception {
        String result = Format.minifyPubkey("fhjskfjjdhdjjkfkdjgfhdhg");
        Assert.assertTrue(result.equals("fhjskf"));

        result = Format.minifyPubkey("fhjs");
        Assert.assertTrue(result.equals("fhjs"));
    }

    @Test
    public void testCreateUri() throws Exception {
        String result = Format.createUri(true,"uid","pubkey","currency");
        Assert.assertTrue(result.equals("pubkey"));

        result = Format.createUri(false,"uid","pubkey","currency");
        Assert.assertTrue(result.equals("duniter://uid:pubkey@currency"));
    }

    @Test
    public void testParseUri() throws Exception {
        String uri = "duniter://uid:pubkey@currency";
        Map<String,String> result = Format.parseUri(uri);

        Assert.assertTrue(
                result.get("uid").equals("uid") && result.get("public_key").equals("pubkey") && result.get("currency").equals("currency"));
    }

    @Test
    public void testIsNull() throws Exception {
        Assert.assertTrue(Format.isNull("salut").equals("salut") && Format.isNull(null).equals("") && Format.isNull("").equals(""));
    }
}