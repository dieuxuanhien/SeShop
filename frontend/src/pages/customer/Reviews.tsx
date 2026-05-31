import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Star } from 'lucide-react';
import { createReview, getProductReviews, uploadReviewImage, type Review } from '@/features/review/api/reviewApi';
import { getMyOrders, type OrderItem } from '@/features/commerce/api/orderApi';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Reviews() {
  const { productId } = useParams<{ productId: string }>();
  const id = Number(productId);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [orderItemId, setOrderItemId] = useState(0);
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [eligibleItems, setEligibleItems] = useState<(OrderItem & { orderNumber: string })[]>([]);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    setIsUploading(true);
    try {
      const url = await uploadReviewImage(file);
      setImageUrl(url);
    } catch {
      setMessage('Failed to upload image.');
    } finally {
      setIsUploading(false);
    }
  }

  async function loadReviews() {
    if (!id) return;
    setIsLoading(true);
    try {
      setReviews(await getProductReviews(id));
    } catch {
      setReviews([]);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadReviews();
    // Load eligible order items for this product
    getMyOrders(1, 100)
      .then((page) => {
        const items: (OrderItem & { orderNumber: string })[] = [];
        for (const order of page.items) {
          if (order.items) {
            for (const item of order.items) {
              items.push({ ...item, orderNumber: order.orderNumber });
            }
          }
        }
        setEligibleItems(items);
      })
      .catch(() => setEligibleItems([]));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!orderItemId) {
      setMessage('Please select a purchased item to review.');
      return;
    }
    setIsSaving(true);
    setMessage('');
    try {
      await createReview({
        orderItemId,
        rating,
        comment,
        imageUrl: imageUrl || undefined,
      });
      setOrderItemId(0);
      setRating(5);
      setComment('');
      setImageUrl('');
      setMessage('Review submitted successfully!');
      await loadReviews();
    } catch {
      setMessage('Review could not be submitted. You may have already reviewed this item.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Product Reviews & Ratings"
      purpose="Read customer feedback and share your experience."
    >
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_360px]">
        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Reviews</h2>
          <div className="mt-4 grid gap-3">
            {isLoading ? (
              <p className="text-sm text-ink/60">Loading reviews...</p>
            ) : reviews.length === 0 ? (
              <EmptyState title="No reviews yet" description="Be the first customer to leave a review for this product." />
            ) : (
              reviews.map((review) => (
                <div key={review.reviewId} className="rounded-md border border-primary/15 bg-ink/[0.03] p-4">
                  <div className="flex items-center gap-1 text-primary">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Star key={index} size={14} className={index < review.rating ? 'fill-primary' : 'text-ink/20'} />
                    ))}
                  </div>
                  <p className="mt-3 text-sm text-ink/75">{review.comment}</p>
                  {review.imageUrl && (
                    <img src={review.imageUrl} alt="Review" className="mt-3 h-24 w-24 rounded-md object-cover border border-primary/20" />
                  )}
                  <p className="mt-2 text-xs text-ink/45">{new Date(review.createdAt).toLocaleString()}</p>
                </div>
              ))
            )}
          </div>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Leave a Review</h2>
          <form onSubmit={handleSubmit} className="mt-4 grid gap-4">
            <div className="flex flex-col gap-1">
              <label htmlFor="review-order-item" className="text-xs font-semibold text-ink/80">
                Select Purchased Item
              </label>
              {eligibleItems.length === 0 ? (
                <p className="text-xs text-ink/50 py-2">
                  You need to have purchased this product to leave a review. Your order items will appear here after purchase.
                </p>
              ) : (
                <select
                  id="review-order-item"
                  value={orderItemId}
                  onChange={(e) => setOrderItemId(Number(e.target.value))}
                  className="w-full rounded-md border border-primary/20 bg-surface p-2.5 text-sm text-ink focus:border-primary focus:outline-none"
                  required
                >
                  <option value={0}>Choose an item from your orders...</option>
                  {eligibleItems.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.productName} {item.skuCode ? `(${item.skuCode})` : ''} — Order {item.orderNumber}
                    </option>
                  ))}
                </select>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs font-semibold text-ink/80">Rating</label>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setRating(star)}
                    onMouseEnter={() => setHoverRating(star)}
                    onMouseLeave={() => setHoverRating(0)}
                    className="p-0.5 transition-transform hover:scale-110"
                    aria-label={`${star} stars`}
                  >
                    <Star
                      size={22}
                      className={`transition-colors ${
                        star <= (hoverRating || rating)
                          ? 'fill-primary text-primary'
                          : 'text-ink/20'
                      }`}
                    />
                  </button>
                ))}
                <span className="ml-2 text-sm text-ink/60">{rating} star{rating !== 1 ? 's' : ''}</span>
              </div>
            </div>

            <label className="grid gap-1">
              <span className="text-xs font-semibold text-ink/80">Comment</span>
              <textarea
                value={comment}
                onChange={(event) => setComment(event.target.value)}
                className="min-h-28 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="Share your experience with this product..."
                required
              />
            </label>
            <label className="grid gap-1">
              <span className="text-xs font-semibold text-ink/80">Image (Optional)</span>
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                disabled={isUploading}
                className="file:mr-4 file:rounded-full file:border-0 file:bg-primary/10 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-primary hover:file:bg-primary/20 text-sm text-ink/75"
              />
              {imageUrl && <p className="text-xs text-success mt-1">Image uploaded successfully</p>}
            </label>
            <Button type="submit" isLoading={isSaving} disabled={isUploading || eligibleItems.length === 0}>Submit Review</Button>
          </form>
          {message ? (
            <p className={`mt-4 text-sm ${message.includes('success') ? 'text-success' : 'text-danger'}`}>{message}</p>
          ) : null}
        </Card>
      </div>
    </PageScaffold>
  );
}
