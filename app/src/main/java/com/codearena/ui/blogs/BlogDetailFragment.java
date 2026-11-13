package com.codearena.ui.blogs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentBlogDetailBinding;

public class BlogDetailFragment extends Fragment {

    private FragmentBlogDetailBinding binding;
    private long blogId = 0;
    private long catalogItemId = 0;
    private String blogTitle = "Reader";
    private String blogAuthor = "";
    private String directUrl = "";
    private boolean isDarkTheme = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBlogDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            blogId = getArguments().getLong("blog_id", 0);
            catalogItemId = getArguments().getLong("catalog_item_id", 0);
            blogTitle = getArguments().getString("blog_title", "Reader");
            blogAuthor = getArguments().getString("blog_author", "");
            directUrl = getArguments().getString("web_url", "");
        }

        binding.toolbarBlogDetail.setTitle(blogTitle);
        if (blogAuthor != null && !blogAuthor.isEmpty()) {
            binding.toolbarBlogDetail.setSubtitle(blogAuthor.startsWith("By ") ? blogAuthor : "By " + blogAuthor);
        }

        binding.toolbarBlogDetail.inflateMenu(R.menu.menu_blog_detail);
        UserPreferencesRepository prefsRepo = new UserPreferencesRepository(requireContext());

        // Initial theme from preferences
        isDarkTheme = prefsRepo.isReaderDarkTheme();
        MenuItem themeMenuItem = binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_theme);
        updateThemeMenuItem(themeMenuItem, isDarkTheme);

        boolean isRead = false;
        if (blogId > 0) {
            prefsRepo.setBlogRead(blogId, true);
            isRead = prefsRepo.isBlogRead(blogId);
        } else if (catalogItemId > 0) {
            isRead = prefsRepo.isCatalogRead(catalogItemId);
        }

        MenuItem readMenuItem = binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_read);
        updateReadMenuItem(readMenuItem, isRead);

        if (blogAuthor != null && !blogAuthor.isEmpty() && !blogAuthor.contains("CSES") && !blogAuthor.contains("Problem Set")) {
            boolean isFriend = prefsRepo.getFriends().contains(blogAuthor);
            binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_friend)
                    .setTitle(isFriend ? "Remove '" + blogAuthor + "' from Friends" : "Add '" + blogAuthor + "' to Friends");
        } else {
            binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_friend).setVisible(false);
        }

        final String targetUrl = (!directUrl.isEmpty()) ? directUrl : ("https://codeforces.com/blog/entry/" + blogId);

        binding.toolbarBlogDetail.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_toggle_theme) {
                isDarkTheme = !isDarkTheme;
                prefsRepo.setReaderDarkTheme(isDarkTheme);
                updateThemeMenuItem(item, isDarkTheme);
                applyTheme(isDarkTheme);
                Toast.makeText(requireContext(),
                        isDarkTheme ? "Black (Dark) Theme" : "White (Light) Theme",
                        Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_toggle_read) {
                if (catalogItemId > 0) {
                    boolean currentRead = prefsRepo.isCatalogRead(catalogItemId);
                    boolean newRead = !currentRead;
                    prefsRepo.setCatalogRead(catalogItemId, newRead);
                    updateReadMenuItem(item, newRead);
                    Toast.makeText(requireContext(), newRead ? "Marked as solved" : "Marked as unsolved", Toast.LENGTH_SHORT).show();
                } else if (blogId > 0) {
                    boolean currentRead = prefsRepo.isBlogRead(blogId);
                    boolean newRead = !currentRead;
                    prefsRepo.setBlogRead(blogId, newRead);
                    updateReadMenuItem(item, newRead);
                    Toast.makeText(requireContext(), newRead ? "Marked as read" : "Marked as unread", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.action_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, blogTitle);
                shareIntent.putExtra(Intent.EXTRA_TEXT, blogTitle + "\n" + targetUrl);
                startActivity(Intent.createChooser(shareIntent, "Share"));
                return true;
            } else if (id == R.id.action_open_browser) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
                    startActivity(browserIntent);
                } catch (Exception ignored) {}
                return true;
            } else if (id == R.id.action_toggle_friend) {
                if (blogAuthor != null && !blogAuthor.isEmpty()) {
                    boolean isFriend = prefsRepo.getFriends().contains(blogAuthor);
                    if (isFriend) {
                        prefsRepo.removeFriend(blogAuthor);
                        item.setTitle("Add '" + blogAuthor + "' to Friends");
                        Toast.makeText(requireContext(), "Removed " + blogAuthor + " from friends", Toast.LENGTH_SHORT).show();
                    } else {
                        prefsRepo.addFriend(blogAuthor);
                        item.setTitle("Remove '" + blogAuthor + "' from Friends");
                        Toast.makeText(requireContext(), "Added " + blogAuthor + " to friends", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            } else if (id == R.id.action_refresh) {
                if (binding != null && binding.webViewBlog != null) {
                    binding.webViewBlog.reload();
                }
                return true;
            }
            return false;
        });

        binding.toolbarBlogDetail.setNavigationOnClickListener(v -> {
            if (binding != null && binding.webViewBlog.canGoBack()) {
                binding.webViewBlog.goBack();
            } else {
                Navigation.findNavController(v).popBackStack();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (binding != null && binding.webViewBlog.canGoBack()) {
                            binding.webViewBlog.goBack();
                        } else {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

        setupWebView();
    }

    private void updateThemeMenuItem(MenuItem item, boolean isDark) {
        if (item == null) return;
        item.setIcon(isDark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
        item.setTitle(isDark ? "Switch to White Theme" : "Switch to Black Theme");
    }

    private void updateReadMenuItem(MenuItem item, boolean isRead) {
        if (item == null || getContext() == null) return;
        item.setTitle(isRead ? "Mark as Unread" : "Mark as Read");
        if (item.getIcon() != null) {
            item.getIcon().setTint(ContextCompat.getColor(requireContext(),
                    isRead ? R.color.verdict_ok : R.color.text_muted_dark));
        }
    }

    private void applyTheme(boolean isDark) {
        if (binding == null || binding.webViewBlog == null) return;

        binding.webViewBlog.setBackgroundColor(isDark ? Color.parseColor("#121212") : Color.parseColor("#FFFFFF"));
        applyThemeJs(isDark);
    }

    private void applyThemeJs(boolean isDark) {
        if (binding == null || binding.webViewBlog == null) return;

        String js;
        if (isDark) {
            js = "javascript:(function() {" +
                    "  try {" +
                    "    var sDark = document.getElementById('styles-dark');" +
                    "    var sLight = document.getElementById('styles');" +
                    "    if (sDark) { sDark.disabled = false; sDark.rel = 'stylesheet'; sDark.removeAttribute('alternate'); }" +
                    "    if (sLight) { sLight.disabled = true; }" +
                    "    if (window.localStorage) { localStorage.setItem('theme', 'dark'); }" +
                    "  } catch(e) {}" +
                    "  var st = document.getElementById('ca-reader-dark');" +
                    "  if (!st) {" +
                    "    st = document.createElement('style');" +
                    "    st.id = 'ca-reader-dark';" +
                    "    st.innerHTML = " +
                    "      'html, body, #body, #pageContent, .skeleton, .content-wrapper, .content, .title-block, .navigation, #header, #footer {' +" +
                    "      '  background-color: #121212 !important; background: #121212 !important; color: #E2E8F0 !important;' +" +
                    "      '}' +" +
                    "      '.roundbox, .roundbox *, .ttypography, .topic, .comment, .info, .caption, .sidebar, .panel, .box, .card, .menu-box {' +" +
                    "      '  background-color: #181818 !important; background: #181818 !important; background-image: none !important;' +" +
                    "      '  color: #E2E8F0 !important; border-color: #2D3748 !important; box-shadow: none !important;' +" +
                    "      '}' +" +
                    "      'div, p, span, h1, h2, h3, h4, h5, h6, li, ul, ol, section, article, header, footer, aside, label {' +" +
                    "      '  color: #E2E8F0 !important;' +" +
                    "      '}' +" +
                    "      'pre, code, .sample-test, .sample-tests, .input, .output, pre *, code * {' +" +
                    "      '  background-color: #1E1E1E !important; background: #1E1E1E !important;' +" +
                    "      '  color: #F87171 !important; border-color: #374151 !important;' +" +
                    "      '}' +" +
                    "      'table, thead, tbody, tr, th, td, .summary-table, .narrow, table.rtable {' +" +
                    "      '  background-color: #181818 !important; background: #181818 !important;' +" +
                    "      '  color: #E2E8F0 !important; border-color: #333333 !important;' +" +
                    "      '}' +" +
                    "      'th, td.title {' +" +
                    "      '  background-color: #222222 !important; background: #222222 !important;' +" +
                    "      '}' +" +
                    "      'a, a *, a:visited {' +" +
                    "      '  color: #60A5FA !important;' +" +
                    "      '}' +" +
                    "      '.MathJax, .katex, .tex-formula, .tex-span {' +" +
                    "      '  color: #F1F5F9 !important;' +" +
                    "      '}' +" +
                    "      'input, textarea, select {' +" +
                    "      '  background-color: #262626 !important; color: #E2E8F0 !important; border-color: #444444 !important;' +" +
                    "      '}' +" +
                    "      'img {' +" +
                    "      '  opacity: 0.88 !important;' +" +
                    "      '}';" +
                    "    (document.head || document.documentElement).appendChild(st);" +
                    "  }" +
                    "})()";
        } else {
            js = "javascript:(function() {" +
                    "  try {" +
                    "    var sDark = document.getElementById('styles-dark');" +
                    "    var sLight = document.getElementById('styles');" +
                    "    if (sDark) { sDark.disabled = true; sDark.setAttribute('alternate', 'alternate'); }" +
                    "    if (sLight) { sLight.disabled = false; }" +
                    "    if (window.localStorage) { localStorage.setItem('theme', 'light'); }" +
                    "  } catch(e) {}" +
                    "  var st = document.getElementById('ca-reader-dark');" +
                    "  if (st) { st.remove(); }" +
                    "  document.documentElement.style.backgroundColor = '';" +
                    "  document.body.style.backgroundColor = '';" +
                    "})()";
        }
        binding.webViewBlog.evaluateJavascript(js, null);
    }

    private void setupWebView() {
        WebSettings settings = binding.webViewBlog.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        applyTheme(isDarkTheme);

        binding.webViewBlog.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (binding != null) {
                    binding.progressBlogDetail.setVisibility(View.VISIBLE);
                }
                applyThemeJs(isDarkTheme);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (binding != null) {
                    binding.progressBlogDetail.setVisibility(View.GONE);
                }
                applyThemeJs(isDarkTheme);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        if (!directUrl.isEmpty()) {
            binding.webViewBlog.loadUrl(directUrl);
        } else if (blogId > 0) {
            binding.webViewBlog.loadUrl("https://codeforces.com/blog/entry/" + blogId);
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.webViewBlog.stopLoading();
            binding.webViewBlog.setWebViewClient(null);
        }
        super.onDestroyView();
        binding = null;
    }
}
