package com.codearena.ui.blogs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.MenuItem;
import androidx.core.content.ContextCompat;
import com.codearena.data.repository.UserPreferencesRepository;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.databinding.FragmentBlogDetailBinding;

public class BlogDetailFragment extends Fragment {

    private FragmentBlogDetailBinding binding;
    private long blogId = 0;
    private String blogTitle = "Codeforces Blog";
    private String blogAuthor = "";

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
            blogTitle = getArguments().getString("blog_title", "Codeforces Blog");
            blogAuthor = getArguments().getString("blog_author", "");
        }

        binding.toolbarBlogDetail.setTitle(blogTitle);
        if (blogAuthor != null && !blogAuthor.isEmpty()) {
            binding.toolbarBlogDetail.setSubtitle("By " + blogAuthor);
        }

        binding.toolbarBlogDetail.inflateMenu(R.menu.menu_blog_detail);
        UserPreferencesRepository prefsRepo = new UserPreferencesRepository(requireContext());

        // Automatically mark as read when opened
        if (blogId > 0) {
            prefsRepo.setBlogRead(blogId, true);
        }

        MenuItem readMenuItem = binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_read);
        updateReadMenuItem(readMenuItem, prefsRepo.isBlogRead(blogId));

        if (blogAuthor != null && !blogAuthor.isEmpty()) {
            boolean isFriend = prefsRepo.getFriends().contains(blogAuthor);
            binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_friend)
                    .setTitle(isFriend ? "Remove '" + blogAuthor + "' from Friends" : "Add '" + blogAuthor + "' to Friends");
        } else {
            binding.toolbarBlogDetail.getMenu().findItem(R.id.action_toggle_friend).setVisible(false);
        }

        binding.toolbarBlogDetail.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_toggle_read) {
                if (blogId > 0) {
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, blogTitle + "\nhttps://codeforces.com/blog/entry/" + blogId);
                startActivity(Intent.createChooser(shareIntent, "Share Blog"));
                return true;
            } else if (id == R.id.action_open_browser) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/blog/entry/" + blogId));
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

    private void updateReadMenuItem(MenuItem item, boolean isRead) {
        if (item == null || getContext() == null) return;
        item.setTitle(isRead ? "Mark as Unread" : "Mark as Read");
        if (item.getIcon() != null) {
            item.getIcon().setTint(ContextCompat.getColor(requireContext(),
                    isRead ? R.color.verdict_ok : R.color.text_muted_dark));
        }
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

        binding.webViewBlog.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (binding != null) {
                    binding.progressBlogDetail.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (binding != null) {
                    binding.progressBlogDetail.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        if (blogId > 0) {
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
