package com.github.vivchar.example.pages.github.items.stargazer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.vivchar.rendererrecyclerviewadapter.ItemModel;
import com.github.vivchar.rendererrecyclerviewadapter.ViewRenderer;
import com.github.vivchar.rendererrecyclerviewadapter.ViewState;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * Created by Vivchar Vitaly on 1/10/17.
 */
public class StargazerViewRenderer extends ViewRenderer<StargazerModel, StargazerViewHolder> {

	@NonNull
	private final Listener mListener;
	private final int mLayout;

	public StargazerViewRenderer(final int type, final int layout, final Context context, @NonNull final Listener listener) {
		super(type, context);
		mLayout = layout;
		mListener = listener;
	}

	@Override
	public void bindView(@NonNull final StargazerModel model, @NonNull final StargazerViewHolder holder) {
		holder.name.setText(model.getName());
		final String url = model.getAvatarUrl();
		Glide.with(getContext())
				.asBitmap()
				.load(url)
				.apply(bitmapTransform(new BlurTransformation(25)))
				.into(holder.avatar);
		holder.check.setVisibility(GONE);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				final boolean willChecked = holder.check.getVisibility() == GONE;
				holder.check.setVisibility(willChecked ? VISIBLE : GONE);
				mListener.onStargazerItemClicked(model, willChecked);
			}
		});

		/* vivchar: temporary workaround */
		holder.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				final boolean willChecked = holder.check.getVisibility() == GONE;
				holder.check.setVisibility(willChecked ? VISIBLE : GONE);
				mListener.onStargazerItemClicked(model, willChecked);
			}
		});
	}

	@NonNull
	@Override
	public StargazerViewHolder createViewHolder(@Nullable final ViewGroup parent) {
		return new StargazerViewHolder(inflate(mLayout, parent));
	}

	@Nullable
	@Override
	public ViewState createViewState(@NonNull final ItemModel model, @NonNull final StargazerViewHolder holder) {
		return new StargazerViewState(model, holder);
	}

	public interface Listener {
		void onStargazerItemClicked(@NonNull StargazerModel model, final boolean isChecked);
	}
}
