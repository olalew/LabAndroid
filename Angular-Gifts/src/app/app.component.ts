import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  public gifts: string[] = []
  public giftInEdition: string = ""

  constructor() {
    this.readList();
  }

  public appendToList() {
    this.getActivity().appendGift(this.giftInEdition);
    this.giftInEdition = "";
    this.readList();
  }

  private readList() {
    let giftString = this.getActivity().getPresentList();
    this.gifts = JSON.parse(!!giftString ? giftString: "[]");
  }
  
  public generate() : void {
    this.getActivity().generate()
  }

  private getActivity(): Activity {
    if (!!(window as any).activity) {
      return (window as any).activity as Activity
    }

    let activity = new Activity()
    activity.appendGift = (gift) => {
      this.gifts.push(gift);
    };
    activity.getPresentList = () => {
      return JSON.stringify(this.gifts)
    }
    return activity
  }
}

export class Activity {
  public getPresentList: () => string | undefined = () => "";
  public appendGift: (gift: string) => void = (gift: String) => {};
  public generate: () => void = () => {}
}
