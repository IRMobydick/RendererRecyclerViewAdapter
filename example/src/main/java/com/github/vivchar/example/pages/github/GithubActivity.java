package com.github.vivchar.example.pages.github;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.vivchar.example.widgets.MyItemDecoration;
import com.github.vivchar.example.R;
import com.github.vivchar.example.pages.github.items.ItemsDiffCallback;
import com.github.vivchar.example.pages.github.items.list.RecyclerViewRenderer;
import com.github.vivchar.example.pages.github.items.category.CategoryModel;
import com.github.vivchar.example.pages.github.items.category.CategoryViewRenderer;
import com.github.vivchar.example.pages.github.items.fork.ForkModel;
import com.github.vivchar.example.pages.github.items.fork.ForkViewRenderer;
import com.github.vivchar.example.pages.github.items.stargazer.StargazerModel;
import com.github.vivchar.example.pages.github.items.stargazer.StargazerViewRenderer;
import com.github.vivchar.network.MainManager;
import com.github.vivchar.rendererrecyclerviewadapter.CompositeViewRenderer;
import com.github.vivchar.rendererrecyclerviewadapter.ItemModel;
import com.github.vivchar.rendererrecyclerviewadapter.RendererRecyclerViewAdapter;
import com.github.vivchar.rendererrecyclerviewadapter.ViewRenderer;

import java.util.ArrayList;

public class GithubActivity extends AppCompatActivity {

	public static final int MAX_SPAN_COUNT = 3;
	private RendererRecyclerViewAdapter mRecyclerViewAdapter;
	private RecyclerView mRecyclerView;
	private GridLayoutManager mLayoutManager;
	private SwipeRefreshLayout mSwipeToRefresh;
	private GithubPresenter mGithubPresenter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.github);
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mGithubPresenter = new GithubPresenter(
				MainManager.getInstance().getStargazersManager(),
				MainManager.getInstance().getForksManager(),
				mMainPresenterView
		);

		mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
		mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mGithubPresenter.onRefresh();
			}
		});

		mRecyclerViewAdapter = new RendererRecyclerViewAdapter();
		mRecyclerViewAdapter.registerRenderer(createStargazerRenderer(R.layout.item_user_full_width));
		mRecyclerViewAdapter.registerRenderer(createCategoryRenderer());
		mRecyclerViewAdapter.registerRenderer(createListRenderer()
				.registerRenderer(createForkRenderer())
				.registerRenderer(createStargazerRenderer(R.layout.item_user_150))
		);

		mLayoutManager = new GridLayoutManager(this, MAX_SPAN_COUNT);
		mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(final int position) {
				switch (mRecyclerViewAdapter.getItemViewType(position)) {
					case ForkModel.TYPE:
					case StargazerModel.TYPE:
						return 1;
					default:
						return 3;
				}
			}
		});

		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(mRecyclerViewAdapter);
		mRecyclerView.addItemDecoration(new MyItemDecoration());
	}

	@NonNull
	private ViewRenderer createForkRenderer() {
		return new ForkViewRenderer(ForkModel.TYPE, this, new Listener());
	}

	@NonNull
	private ViewRenderer createStargazerRenderer(final int layout) {
		return new StargazerViewRenderer(StargazerModel.TYPE, layout, this, new Listener());
	}

	@NonNull
	private ViewRenderer createCategoryRenderer() {
		return new CategoryViewRenderer(CategoryModel.TYPE, this, new Listener());
	}

	@NonNull
	private CompositeViewRenderer createListRenderer() {
		return new RecyclerViewRenderer(this);
	}

//	@NonNull
//	private CompositeViewRenderer createStargazersRenderer() {
//		return new RecyclerViewRenderer(this);
//	}


	@Override
	protected void onStart() {
		super.onStart();
		mGithubPresenter.viewShown();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGithubPresenter.viewHidden();
	}

	@NonNull
	private final GithubPresenter.View mMainPresenterView = new GithubPresenter.View() {

		@Override
		public void updateList(@NonNull final ArrayList<ItemModel> list) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mRecyclerViewAdapter.setItems(list, new ItemsDiffCallback());
				}
			});
		}

		@Override
		public void showProgressView() {
			mSwipeToRefresh.setRefreshing(true);
		}

		@Override
		public void hideProgressView() {
			mSwipeToRefresh.setRefreshing(false);
		}

		@Override
		public void showMessageView(@NonNull final String message, @NonNull final String url) {
			final View view = getWindow().getDecorView().findViewById(android.R.id.content);
			Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction(R.string.view, new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
			}).show();
		}

		@Override
		public void showMessageView(@NonNull final String message) {
			final View view = getWindow().getDecorView().findViewById(android.R.id.content);
			Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
		}

		@Override
		public Context getContext() {
			return GithubActivity.this;
		}
	};

	private class Listener implements StargazerViewRenderer.Listener, CategoryViewRenderer.Listener, ForkViewRenderer.Listener {

		@Override
		public void onStargazerItemClicked(@NonNull final StargazerModel model) {
			mGithubPresenter.onStargazerClicked(model);
		}

		@Override
		public void onCategoryClicked(@NonNull final CategoryModel model) {
			mGithubPresenter.onCategoryClicked(model);
		}

		@Override
		public void onForkItemClicked(@NonNull final ForkModel model) {
			mGithubPresenter.onForkClicked(model);
		}
	}
}
