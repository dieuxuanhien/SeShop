import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Star } from 'lucide-react';
import { createReview, getProductReviews, type Review } from '@/features/review/api/reviewApi';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';

export function Reviews() {
  const { productId } = useParams<{ productId: string }>();
  const id = Number(productId);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [orderItemId, setOrderItemId] = useState(0);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

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
  }, [id]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
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
      setMessage('Review submitted.');
      await loadReviews();
    } catch {
      setMessage('Review could not be submitted.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Product Reviews & Ratings"
      viewCode="CUST_007"
      purpose="Read customer feedback and submit a verified order-item review."
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
                  <p className="mt-2 text-xs text-ink/45">{new Date(review.createdAt).toLocaleString()}</p>
                </div>
              ))
            )}
          </div>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Leave a Review</h2>
          <form onSubmit={handleSubmit} className="mt-4 grid gap-4">
            <Input
              label="Order Item ID"
              type="number"
              min={1}
              value={orderItemId || ''}
              onChange={(event) => setOrderItemId(Number(event.target.value))}
              required
            />
            <Select
              label="Rating"
              value={String(rating)}
              onChange={(event) => setRating(Number(event.target.value))}
              options={[5, 4, 3, 2, 1].map((value) => ({ label: `${value} stars`, value: String(value) }))}
            />
            <label className="grid gap-1 text-sm font-medium text-ink">
              <span>Comment</span>
              <textarea
                value={comment}
                onChange={(event) => setComment(event.target.value)}
                className="min-h-28 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                required
              />
            </label>
            <Input label="Image URL" value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} />
            <Button type="submit" isLoading={isSaving}>Submit Review</Button>
          </form>
          {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
        </Card>
      </div>
    </PageScaffold>
  );
}
