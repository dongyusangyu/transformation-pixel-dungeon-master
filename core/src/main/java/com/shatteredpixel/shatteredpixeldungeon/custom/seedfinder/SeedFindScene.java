package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeedFindScene extends PixelScene {

    public ScrollPane list;
    public String s;
    public static CreditsBlock txt;
    public static RenderedTextBlock r;
    public boolean stop;
    public static Thread thread;
    private static volatile boolean closing;

    public WndTextInput wndTextInput;

    @Override
    public void create() {
        super.create();

        final float colWidth = 120;
        final float fullWidth = colWidth * (landscape() ? 2 : 1);

        int w = Camera.main.width;
        int h = Camera.main.height;

        s = null;
        closing = false;

        //Archs archs = new Archs();
        //archs.setSize(w, h);
        //add(archs);
        TitleBackground BG = new TitleBackground(w, h);
        add( BG );

        //darkens the arches
        add(new ColorBlock(w, h, 0x88000000));

        list = new ScrollPane(new Component());
        add(list);

        Component content = list.content();
        content.clear();

        ShatteredPixelDungeon.scene().addToFront( wndTextInput = new WndTextInput(Messages.get(this, "title"), Messages.get(this, "body"), Messages.get(this, "initial_value"), 1000, true, Messages.get(this, "find"), Messages.get(this, "clear")) {
            @Override
            public void onSelect(boolean positive, String text) {
                int floor = 26;
                boolean floorOption = false;
                String talent = "Negative Talent";
                String up_to_floor = "floor end";
                String strFloor = "floor";

                if (text.contains(up_to_floor)) {
                    floorOption = true;
                    String fl = text.split(strFloor)[0].trim();
                    try {
                        floor = Integer.parseInt(fl);
                    } catch (NumberFormatException e) {
                    }
                }

                if (positive && !text.isEmpty() && floorOption) {
                    String[] itemList = floorOption ? Arrays.copyOfRange(text.split("\n"), 1, text.split("\n").length) : text.split("\n");

                    Component content = list.content();
                    content.clear();

                    r = PixelScene.renderTextBlock("abc",9);
                    r.maxWidth(w - 40);
                    r.setPos(20,20);
                    ShatteredPixelDungeon.scene().addToFront(r);

                    final int finalFloor = floor;
                    thread = new Thread(() -> {
                        SeedFinder finder = new SeedFinder();
                        s = finder.findSeed(itemList, finalFloor, state -> Gdx.app.postRunnable(() -> {
                            if (!closing && thread != null && r != null) {
                                r.text(state.summary());
                            }
                        }));
                        Gdx.app.postRunnable(() -> {
                            if (closing) {
                                ShatteredPixelDungeon.switchNoFade(TitleScene.class);
                                return;
                            }

                            if (r != null) r.destroy();

                            txt = new CreditsBlock(true, Window.TITLE_COLOR, s);
                            txt.setRect((Camera.main.width - colWidth)/2f, 12, colWidth, 0);

                            content.add(txt);
                            content.setSize( fullWidth, txt.bottom()+10 );

                            if (list.isActive()) {
                                list.setRect( 0, 0, w, h );
                                list.scrollTo(0, 0);
                            }

                            // 新增：自动复制种子到剪贴板
                            copySeedToClipboard(s, content, fullWidth, txt);

                        });
                    });
                    thread.start();

                } else if (!positive && !text.isEmpty()) {
                    text = DungeonSeed.formatText(text);
                    long seed = DungeonSeed.convertFromText(text);

                    RenderedTextBlock renderedTextBlock = PixelScene.renderTextBlock(new SeedFinder().logSeedItems(Long.toString(seed),26),9);
                    renderedTextBlock.setRect((Camera.main.width - colWidth)/2f, 12, colWidth, 0);
                    content.add(renderedTextBlock);
                    content.setSize( fullWidth, renderedTextBlock.bottom()+10 );
                    list.setRect( 0, 0, w, h );
                    list.scrollTo(0, 0);



                }else {
                    SPDSettings.customSeed("");
                    ShatteredPixelDungeon.switchNoFade(TitleScene.class);
                }
            }
        });

        RedButton btnStop = new RedButton(Messages.get(this, "stop")) {
            @Override
            protected void onClick() {
                if (thread != null && thread.isAlive()) {
                    closing = false;
                    SeedFinder.findingStatus = SeedFinder.FINDING.STOP;
                    if (r != null) {
                        r.text(Messages.get(SeedFindScene.class, "stopping"));
                    }
                }
            }
        };
        btnStop.setRect(0, Camera.main.height - 18, 80, 16);
        add(btnStop);

        ExitButton btnExit = new ExitButton() {
            @Override
            protected void onClick() {
                closing = true;
                SeedFinder.findingStatus = SeedFinder.FINDING.STOP;
                if (thread == null || !thread.isAlive()) {
                    ShatteredPixelDungeon.switchNoFade(TitleScene.class);
                    System.gc();
                } else if (r != null) {
                    r.text(Messages.get(SeedFindScene.class, "leaving"));
                }
            }
        };
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);

        //fadeIn();
    }

    // 新增：提取种子编码并复制到剪贴板
    private void copySeedToClipboard(String seedResult, Component content, float fullWidth, CreditsBlock txt) {
        if (seedResult == null || seedResult.isEmpty() || seedResult.equals("NONE")) {
            return;
        }
        // 提取种子编码（优先提取可输入的编码，再提取原始数字种子）
        String seedCode = extractSeedCode(seedResult);
        //String seedCode = seedResult;
        if (seedCode != null && !seedCode.isEmpty()) {
            // 复制到剪贴板
            Gdx.app.getClipboard().setContents(seedCode);
            // 显示复制成功提示
            RenderedTextBlock copyHint = PixelScene.renderTextBlock(Messages.get(SeedFindScene.class, "copy_success"), 9);
            copyHint.setPos((Camera.main.width - copyHint.width())/2f, txt.bottom() + 10);
            //content.add(copyHint);
            content.setSize(fullWidth, copyHint.bottom() + 10);
        }
    }

    // 新增：从结果字符串中解析种子编码
    private String extractSeedCode(String resultStr) {
        // 匹配 "seed XXXXX \n(123456) items:" 格式中的编码
        String seedKey = Messages.get(SeedFinder.class, "seed");
        Pattern pattern = Pattern.compile(seedKey + "\\s+([^\\s]+)");
        Matcher matcher = pattern.matcher(resultStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 匹配原始数字种子（括号内的数字）
        Pattern rawPattern = Pattern.compile("\\((\\d+)\\)");
        Matcher rawMatcher = rawPattern.matcher(resultStr);
        if (rawMatcher.find()) {
            return rawMatcher.group(1);
        }
        return null;
    }

    public boolean isAllDigits(String str) {
        return str != null && str.matches("\\d+");
    }

    private void addLine(float y, Group content) {
        ColorBlock line = new ColorBlock(Camera.main.width, 1, 0xFF333333);
        line.y = y;
        content.add(line);
    }

    @Override
    public void update() {
        super.update();
    }

    public static class CreditsBlock extends Component {

        boolean large;

        public RenderedTextBlock body;

        public CreditsBlock(boolean large, int highlight, String body) {
            super();

            this.large = large;

            this.body = PixelScene.renderTextBlock(body, 6);
            if (highlight != -1)
                this.body.setHightlighting(true, highlight);
            if (large)
                this.body.align(RenderedTextBlock.CENTER_ALIGN);
            add(this.body);
        }

        @Override
        protected void layout() {
            super.layout();

            float topY = top();

            if (large){
                body.maxWidth((int)width());
                body.setPos( x + (width() - body.width())/2f, topY);
            } else {
                topY += 1;
                body.maxWidth((int)width());
                body.setPos( x, topY);
            }

            topY += body.height();

            height = Math.max(height, topY - top());
        }

    }
}