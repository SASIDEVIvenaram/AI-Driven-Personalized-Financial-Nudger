import type { Banner as BannerType } from '../types'

export function Banner({ banner }: { banner: BannerType | null }) {
  if (!banner) return null
  return <div className={`banner ${banner.type}`}>{banner.message}</div>
}
